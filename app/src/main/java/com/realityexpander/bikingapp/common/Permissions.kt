package com.realityexpander.bikingapp.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.Size
import androidx.core.content.ContextCompat
import timber.log.Timber

class Permissions {

    companion object {
        // Checks for necessary location permissions
        fun hasLocationPermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                hasPermissions(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } else {
                hasPermissions(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }

        private fun hasPermissions(
            context: Context,
            @Size(min = 1) vararg perms: String,
        ): Boolean {
            // Always return true for SDK < M, let the system deal with the permissions
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Timber.w("Permissions - hasPermissions: API version < M, returning true by default")

                // DANGER ZONE!!! Don't Change this!
                return true
            }

            for (perm in perms) {
                if (ContextCompat.checkSelfPermission(context, perm)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }
    }
}