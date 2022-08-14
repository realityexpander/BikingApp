package com.realityexpander.bikingapp.common

import android.location.Location
import java.util.concurrent.TimeUnit

class TrackingUtility {

    companion object {

        // Converts milliseconds to HH:MM:SS string, optionally with milliseconds HH:MM:SS.mm
        fun getFormattedStopWatchTime(
            ms: Long,
            includeMillis: Boolean = false,
        ): String {
            var milliseconds = ms
            val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
            milliseconds -= TimeUnit.HOURS.toMillis(hours)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
            milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

            if (!includeMillis) {
                return  "${if (hours < 10) "0"   else ""}${hours}h " +
                        "${if (minutes < 10) "0" else ""}${minutes}m " +
                        "${if (seconds < 10) "0" else ""}${seconds}s"
            }

            milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
            milliseconds /= 10

            return  "${if (hours < 10) "0"        else ""}$hours:" +
                    "${if (minutes < 10) "0"      else ""}$minutes:" +
                    "${if (seconds < 10) "0"      else ""}$seconds." +
                    "${if (milliseconds < 10) "0" else ""}$milliseconds"
        }

        // Calculate length of polyline (list of lat/long points) in meters
        fun calculatePolylineLength(polyline: PolyLineOfLatLngs): Float {
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