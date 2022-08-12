package com.realityexpander.bikingapp.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.realityexpander.bikingapp.R
import com.realityexpander.bikingapp.db.Ride
import com.realityexpander.bikingapp.common.Constants.Companion.ACTION_PAUSE_SERVICE
import com.realityexpander.bikingapp.common.Constants.Companion.ACTION_START_OR_RESUME_SERVICE
import com.realityexpander.bikingapp.common.Constants.Companion.ACTION_STOP_SERVICE
import com.realityexpander.bikingapp.common.Constants.Companion.MAP_VIEW_BUNDLE_KEY
import com.realityexpander.bikingapp.common.Constants.Companion.MAP_ZOOM
import com.realityexpander.bikingapp.common.Constants.Companion.POLYLINE_COLOR
import com.realityexpander.bikingapp.common.Constants.Companion.POLYLINE_WIDTH
import com.realityexpander.bikingapp.common.TrackingUtility
import com.realityexpander.bikingapp.services.TrackingService
import com.realityexpander.bikingapp.ui.RideViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.round
import com.realityexpander.bikingapp.common.PolyLineOfLatLngs

const val CANCEL_DIALOG_TAG = "CancelDialog"

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking), GoogleMap.OnMapLoadedCallback {

    // works with no Named
//    @set:Inject
//    var weight: Float = 0f

    // DOES NOT WORK WITH Named
//    @set:[Inject Named("weight")]
//    var weight: Float = 0f

    // works with Named annotation
    @JvmField
    @field:[Inject Named("weight")]
    var weight: Float = 0f

    // works with custom annotation
//    @JvmField
//    @field:[Inject Weight]
//    protected var weight: Float = 0f // works with protected

    @JvmField
    @field:[Inject Named("height")]
    protected var height: Float = 0f

    @Inject
    @Named("String1")
    lateinit var string1: String



    private var map: GoogleMap? = null

    private var isTracking = false
    private var curElapsedRideTimeInMillis = 0L
    private var pathSegments = mutableListOf<PolyLineOfLatLngs>()

    private val viewModel: RideViewModel by viewModels()

    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)

        // restore dialog instance (if needed) after configuration change
        if(savedInstanceState != null) {
            val cancelRideDialog =
                parentFragmentManager.findFragmentByTag(CANCEL_DIALOG_TAG) as CancelRideDialog?
            cancelRideDialog?.setYesListener {
                stopRide()
            }
        }

        btnToggleRideActive.setOnClickListener {
            // Delayed to allow the UI to update for click highlight effect
            Handler(Looper.getMainLooper()).postDelayed({
                toggleRideActive()
            }, 150)
        }

        btnFinishRide.setOnClickListener {
            zoomToWholeTrack()
            endRideAndSaveToDB()
        }

        // Get the map
        mapView.getMapAsync {
            map = it
            addAllPolylinesToMap()
        }
        subscribeToObservers()


        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.toolbar_menu_tracking, menu)
                this@TrackingFragment.menu = menu
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.miCancelTracking -> {
                        showCancelTrackingDialog()
                        true
                    }
                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)

                if (curElapsedRideTimeInMillis > 0L) {
                    menu.getItem(0)?.isVisible = true // cancel tracking
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED) // only show menu when resumed

    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTrackingUI(it)
        })

        TrackingService.pathSegments.observe(viewLifecycleOwner, Observer { segments ->
            pathSegments = segments

            addLatestPolylineToMap()
            moveCameraToUserMostRecentLocation()
        })

        TrackingService.rideTimeElapsedInMillis.observe(viewLifecycleOwner, Observer {
            curElapsedRideTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(it, true)
            tvTimer.text = formattedTime
        })
    }

    private fun moveCameraToUserMostRecentLocation() {
        if (pathSegments.isNotEmpty() && pathSegments.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathSegments.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    // Adds all current pathSegments to map (after configuration change)
    private fun addAllPolylinesToMap() {
        for (polyline in pathSegments) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolylineToMap() {
        // only add polyline if there are at least two elements in the last polyline
        if (pathSegments.isNotEmpty() && pathSegments.last().size > 1) {
            val prevLastLatLng = pathSegments.last()[pathSegments.last().size - 2]
            val lastLatLng = pathSegments.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(prevLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun updateTrackingUI(isTracking: Boolean) {
        this.isTracking = isTracking

        if (!isTracking && curElapsedRideTimeInMillis > 0L) {
            btnToggleRideActive.text = getString(R.string.continue_text)
            btnFinishRide.visibility = View.VISIBLE
        } else if (isTracking) {
            btnToggleRideActive.text = getString(R.string.pause_text)
            menu?.getItem(0)?.isVisible = true
            btnFinishRide.visibility = View.GONE
        }
    }

    private fun toggleRideActive() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            pauseTrackingService()
            Timber.d("Paused tracking service")
        } else {
            startOrResumeTrackingService()
            Timber.d("Started tracking service")
        }
    }

    private fun startOrResumeTrackingService() =
        Intent(requireContext(), TrackingService::class.java).also { intent ->
            intent.action = ACTION_START_OR_RESUME_SERVICE
            requireContext().startService(intent)  // deliver intent to the service
        }

    private fun pauseTrackingService() =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = ACTION_PAUSE_SERVICE
            requireContext().startService(it)  // send message to the service
        }

    private fun stopTrackingService() =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = ACTION_STOP_SERVICE
            requireContext().startService(it)
        }

    override fun onSaveInstanceState(outState: Bundle) {
        val mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)

        mapViewBundle?.let { mapView?.onSaveInstanceState(mapViewBundle) }
    }

    // Zoom out until the whole track is visible.
    // Used to set screenshot of MapView for database.
    private fun zoomToWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathSegments) {
            for (point in polyline) {
                bounds.include(point)
            }
        }
        val width = mapView.width
        val height = mapView.height

        // Dont use animation, we want to see the whole ride track immediately
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                width,
                height,
                (height * 0.05f).toInt()  // padding to show whole track
            )
        )
    }

    //Save the recent ride database and end ride.
    private fun endRideAndSaveToDB() {

        val callback: SnapshotReadyCallback = object : SnapshotReadyCallback {
            var bmp: Bitmap? = null
            override fun onSnapshotReady(snapshot: Bitmap?) {
                bmp = snapshot

                getScreenShotFromView(mapView, activity!!) { bmp2 ->

                    // Collect the data for the ride
                    var distanceInMeters = 0
                    for (polyline in pathSegments) {
                        distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
                    }
                    val avgSpeed =
                        round((distanceInMeters / 1000f) /
                                (curElapsedRideTimeInMillis / 1000f / 60 / 60) * 10) / 10f
                    val timestamp = Calendar.getInstance().timeInMillis
                    val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
                    val ride =
                        Ride(bmp,
                            timestamp,
                            avgSpeed,
                            distanceInMeters,
                            curElapsedRideTimeInMillis,
                            caloriesBurned
                        )

                    // Add Ride to database
                    viewModel.insertRide(ride)

                    Snackbar.make(
                        requireActivity().findViewById(R.id.rootView),  // Prevents crash by using the rootView which is the activity's view
                        "Ride saved successfully.",
                        Snackbar.LENGTH_LONG
                    ).show()

                    stopRide()
                }
            }

        }
        map?.setOnMapLoadedCallback {
            // Toast.makeText(requireContext(), "Saving...", Toast.LENGTH_SHORT).show()
            map?.snapshot(callback)
        }

    }

    // Finish tracking & return to home screen.
    private fun stopRide() {
        Timber.d("Ride stopped")
        tvTimer.text = "00:00:00.00"
        stopTrackingService()

        findNavController().navigate(R.id.action_trackingFragment_to_rideFragment2)
    }

    private fun showCancelTrackingDialog() {
        CancelRideDialog().apply {
            setYesListener {
                stopRide()
            }
        }.show(parentFragmentManager, CANCEL_DIALOG_TAG)
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    fun getScreenShotFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        activity.window?.let { window ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PixelCopy.request(
                        window,
                        Rect(
                            locationOfViewInWindow[0],
                            locationOfViewInWindow[1],
                            locationOfViewInWindow[0] + view.width,
                            locationOfViewInWindow[1] + view.height
                        ), bitmap, { copyResult ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                callback(bitmap) }
                            else {

                            }
                            // possible to handle other result codes ...
                        },
                        Handler(Looper.getMainLooper())
                    )
                }
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                e.printStackTrace()
            }
        }
    }

    override fun onMapLoaded() {
//        if (isFirstLoad) {  // todo fix this
//            isFirstLoad = false
//            animateCamera(curLatLng)
//        }
        Toast.makeText(context, "Map Loaded", Toast.LENGTH_SHORT).show()
    }
}