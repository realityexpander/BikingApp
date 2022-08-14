package com.realityexpander.bikingapp.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.bikingapp.R
import com.realityexpander.bikingapp.adapters.RideAdapter
import com.realityexpander.bikingapp.common.Constants.Companion.REQUEST_CODE_LOCATION_PERMISSION
import com.realityexpander.bikingapp.common.SortType
import com.realityexpander.bikingapp.common.TrackingUtility
import com.realityexpander.bikingapp.ui.viewmodels.RideViewModel
import com.google.android.material.snackbar.Snackbar
import com.realityexpander.bikingapp.common.Constants
import com.realityexpander.bikingapp.common.Constants.Companion.REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ride.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class RideFragment : Fragment(R.layout.fragment_ride) { //, EasyPermissions.PermissionCallbacks {

    lateinit var rideAdapter: RideAdapter

    private val viewModel: RideViewModel by viewModels()

    var sortTypePref: SortType = SortType.DATE
        set(sortType) {
            field = sortType
            viewModel.sortRides(sortType)
            writeSortTypeToSharedPref(sortType)
        }

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestLocationPermissions()

        // Load sort type from sharedPrefs
        sortTypePref = getSortTypeFromSharedPref()

        // Start new tracking
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_rideFragment_to_trackingFragment)
        }

        // Setup the current sort type selection for spinner
        spSortType.setSelection(sortTypePref.ordinal)

        // Setup the sort type spinner selection listener
        spSortType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                sortTypePref = SortType.values()[pos]
            }
        }

        // Setup the recycler view
        rideAdapter = RideAdapter()
        setupRecyclerView()
        viewModel.rides.observe(viewLifecycleOwner, Observer { rides ->
            rideAdapter.submitList(rides)
        })
    }

    private fun setupRecyclerView() = rvRides.apply {
        adapter = rideAdapter
        layoutManager = LinearLayoutManager(activity)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this)
    }

    // Handle swipe-to-delete
    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val ride = rideAdapter.differ.currentList[position]

            viewModel.deleteRide(ride)
            Snackbar.make(requireView(), "Successfully deleted ride", Snackbar.LENGTH_LONG).apply {
                setAction("Undo") {
                    viewModel.insertRide(ride)
                }
                show()
            }
        }
    }

    //////////////////////////////////////////
    ///// EasyPermissions                /////

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

        var grantedCount = 0
        var isCancelled = false

        permissions.entries.forEach {
            // check whether each permission is granted or not
            val permissionName = it.key
            val isGranted = it.value

            if (isGranted) {
                // Permission is granted
                grantedCount++
            } else {
                // Permission is denied
                //Snackbar.make(requireView(), "Permission $permissionName is denied", Snackbar.LENGTH_LONG).show()

//                // Show alert dialog
//                AppSettingsDialog.Builder(this)
//                    .setThemeResId(R.style.AlertDialogTheme)
//                    .build()
//                    .show()

                // Show permission denied dialog
                val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                builder.setTitle("Permission Denied")
                builder.setMessage("Without location permission this app will not operate properly.\n\n" +
                        "Restart the app to grant the permission.")
                builder.setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                }
                builder.show()

                return@registerForActivityResult
            }
        }

        if (grantedCount == permissions.size) {
            // COARSE and FINE permissions have been granted
            // request background location permission, only on API >= Q
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Show request rationalization dialog

                // show simple alert dialog
                val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                builder.setTitle("Request Permission")
                builder.setMessage("This app needs background location permission to track your rides.\n\n" +
                        "Please grant ALLOW ALL THE TIME.")
                builder.setPositiveButton("OK") { dialog, which ->
                    requestBackgroundLocationPermission()
                }
                builder.setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()

                    isCancelled = true

                    // Show permission denied dialog
                    val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                    builder.setTitle("Permission Denied")
                    builder.setMessage("Without location permission this app will not operate properly.\n\n" +
                            "Restart the app to grant the permission.")
                    builder.setPositiveButton("OK") { dialog, which ->
                        dialog.dismiss()
                    }
                    builder.show()
                }
                builder.show()
            }
        }

        if (isCancelled) {
            return@registerForActivityResult
        }
    }

    private val requestSinglePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission is granted
             Snackbar
                 .make(requireView(), "Location permissions granted", Snackbar.LENGTH_SHORT)
                 .show()
        } else {
//            // Show App settings for permissions
//            AppSettingsDialog.Builder(this)
//                .setThemeResId(R.style.AlertDialogTheme)
//                .build()
//                .show()

//            // Open app settings
//             val intent = Intent()
//             intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//             val uri = Uri.fromParts("package", requireContext().packageName, null)
//             intent.data = uri
//             startActivity(intent)

            // Show permission denied dialog
            val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
            builder.setTitle("Permission Denied")
            builder.setMessage("Without background location permission allowed ALL THE TIME this app will not operate properly.\n\n" +
                    "Restart the app to grant the permission.")
            builder.setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }
            builder.show()

            return@registerForActivityResult
        }
    }

    private fun requestLocationPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }

        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                //Manifest.permission.ACCESS_BACKGROUND_LOCATION  // this is only available on Q, and done in second step
            ))

//        // Ask for FINE and COARSE location permissions first.
//        EasyPermissions.requestPermissions(
//            this,
//            "Please accept location permissions to use this app",
//            REQUEST_CODE_LOCATION_PERMISSION,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {

        requestSinglePermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

//    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
//        // If permissions are denied, allow user to choose to go to app settings to manually grant permissions.
//        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
//            AppSettingsDialog.Builder(this)
//                .setThemeResId(R.style.AlertDialogTheme)
//                .build()
//                .show()
//        } else {
//            requestLocationPermissions()
//        }
//    }
//
//    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
//
//        // FOR VERSION_CODE >= Q, we need to request background location permission as well.
//        // After accepting the COARSE and FINE location permission,
//        //   we must ask for BACKGROUND_LOCATION permission to be enabled.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            if(requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
//                EasyPermissions.requestPermissions(
//                    this,
//                    "Please accept ALLOW ALL THE TIME location permissions to use this app",
//                    REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION,
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION  // must be asked after ACCESS_FINE_LOCATION & ACCESS_COARSE_LOCATION
//                )
//            }
//        }
//    }
//
//    // Pass this android system callback to EasyPermissions to handle the result of the permission request.
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
//    }


    //////////////////////////////////////
    // Shared Preferences ////////////////

    private fun writeSortTypeToSharedPref(type: SortType) {
        sharedPref
            .edit()
            .putInt(Constants.KEY_SORT_TYPE, type.ordinal)
            .apply() // .commit() is synchronous, .apply() is asynchronous
    }

    private fun getSortTypeFromSharedPref(): SortType {
        return SortType.values()[sharedPref.getInt(Constants.KEY_SORT_TYPE, 0)]
    }
}