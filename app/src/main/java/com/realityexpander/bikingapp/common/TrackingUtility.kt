package com.realityexpander.bikingapp.common

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.realityexpander.bikingapp.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

class TrackingUtility {

    companion object {
        // Checks for necessary location permissions
        fun hasLocationPermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                EasyPermissions.hasPermissions(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } else {
                EasyPermissions.hasPermissions(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }

        // Converts milliseconds to HH:MM:SS string, optionally with milliseconds HH:MM:SS.mm
        fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
            var milliseconds = ms
            val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
            milliseconds -= TimeUnit.HOURS.toMillis(hours)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
            milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

            if (!includeMillis) {
                return  "${if (hours < 10) "0"   else ""}$hours:" +
                        "${if (minutes < 10) "0" else ""}$minutes:" +
                        "${if (seconds < 10) "0" else ""}$seconds"
            }

            milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
            milliseconds /= 10

            return  "${if (hours < 10) "0"        else ""}$hours:" +
                    "${if (minutes < 10) "0"      else ""}$minutes:" +
                    "${if (seconds < 10) "0"      else ""}$seconds." +
                    "${if (milliseconds < 10) "0" else ""}$milliseconds"
        }

        // Calculate length of polyline (list of lat/long points) in meters
        fun calculatePolylineLength(polyline: Polyline): Float {
            var distance = 0f

            for (i in 0..polyline.size - 2) {
                val pointStart = polyline[i]
                val pointEnd = polyline[i + 1]
                val result = FloatArray(1)

                Location.distanceBetween(
                    pointStart.latitude,
                    pointStart.longitude,
                    pointEnd.latitude,
                    pointEnd.longitude,
                    result  // in meters
                )

                distance += result[0]
            }

            return distance
        }
    }
}