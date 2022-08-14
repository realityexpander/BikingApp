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
import com.realityexpander.bikingapp.common.Permissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ride.*
//import pub.devrel.easypermissions.AppSettingsDialog
//import pub.devrel.easypermissions.EasyPermissions
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
    ///// Permissions                /////

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

        var grantedCount = 0
        var isCancelled = false

        permissions.entries.forEach {
            // check whether each permission is granted or not
            val permissionName = it.key.split(".")[2]
            val isGranted = it.value

            if (isGranted) {
                // Permission is granted
                grantedCount++
            }
            else {
                // Permission is denied
                //Snackbar.make(requireView(), "Permission $permissionName is denied", Snackbar.LENGTH_LONG).show()

                // Show permission denied dialog
                val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                builder.setTitle("Permission Denied")
                builder.setMessage("Without $permissionName this app will not operate properly.\n\n" +
                        "Please restart the app to grant the permission.")
                builder.setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                }
                builder.show()

                return@registerForActivityResult
            }
        }

        // Have COARSE_LOCATION and FINE_LOCATION permissions have been granted?
        if (grantedCount == permissions.size) {

            // Request `background location` permission, only on API >= Q
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Show request rationalization dialog

                // show simple alert dialog
                val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                builder.setTitle("Request Permission")
                builder.setMessage("This app needs background location permission to track your rides.\n\n" +
                        "Please tap OK, and accept ALLOW ALL THE TIME location permission on the next screen.")
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
                            "Please restart the app to grant the permission.")
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

            // Show permission denied dialog
            val builder = android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
            builder.setTitle("Permission Denied")
            builder.setMessage("Without background location permission ALLOWED ALL THE TIME this app will not operate properly.\n\n" +
                    "Please restart the app to grant the permission.")
            builder.setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }
            builder.show()

            return@registerForActivityResult
        }
    }

    private fun requestLocationPermissions() {
        if (Permissions.hasLocationPermissions(requireContext())) {
            return
        }

        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ))

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {

        requestSinglePermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }


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