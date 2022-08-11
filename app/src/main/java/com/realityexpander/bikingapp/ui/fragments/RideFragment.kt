package com.realityexpander.bikingapp.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
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
import com.realityexpander.bikingapp.ui.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ride.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RideFragment : Fragment(R.layout.fragment_ride), EasyPermissions.PermissionCallbacks {

    lateinit var rideAdapter: RideAdapter

    private val viewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewModel = (activity as MainActivity).mainViewModel

        requestPermissions()

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_rideFragment_to_trackingFragment)
        }

        // Setup the current sort type selection for spinner
//        when (viewModel.sortType) {
//            SortType.DATE -> spSortType.setSelection(0)
//            SortType.BIKING_TIME -> spSortType.setSelection(1)
//            SortType.DISTANCE -> spSortType.setSelection(2)
//            SortType.AVG_SPEED -> spSortType.setSelection(3)
//            SortType.CALORIES_BURNED -> spSortType.setSelection(4)
//        }
        spSortType.setSelection(viewModel.sortType.value?.ordinal ?: 0)

        // Setup the sort type spinner selection listener
        spSortType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                println("onNothingSelected")
            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
//                when (pos) {
//                    0 -> viewModel.sortRides(SortType.DATE)
//                    1 -> viewModel.sortRides(SortType.BIKING_TIME)
//                    2 -> viewModel.sortRides(SortType.DISTANCE)
//                    3 -> viewModel.sortRides(SortType.AVG_SPEED)
//                    4 -> viewModel.sortRides(SortType.CALORIES_BURNED)
//                }
                viewModel.sortRides(SortType.values()[pos])
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

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permission to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
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

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        // If permissions are denied, allow user to choose to go to app settings to manually grant permissions.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                .setThemeResId(R.style.AlertDialogTheme)
                .build()
                .show()
        } else {
            requestPermissions()
        }
    }

    //////////////////////////////////////////
    ///// Callbacks from EasyPermissions /////

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    // Pass this android system callback to EasyPermissions to handle the result of the permission request.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}