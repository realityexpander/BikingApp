package com.realityexpander.bikingapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.realityexpander.bikingapp.R
import com.realityexpander.bikingapp.common.Constants
import com.realityexpander.bikingapp.common.Constants.Companion.ACTION_PAUSE_SERVICE
import com.realityexpander.bikingapp.common.Constants.Companion.ACTION_START_OR_RESUME_SERVICE
import com.realityexpander.bikingapp.common.Constants.Companion.ACTION_STOP_SERVICE
import com.realityexpander.bikingapp.common.Constants.Companion.LOCATION_UPDATE_FASTEST_INTERVAL
import com.realityexpander.bikingapp.common.Constants.Companion.LOCATION_UPDATE_INTERVAL
import com.realityexpander.bikingapp.common.Constants.Companion.LOCATION_UPDATE_MAXIMUM_WAIT_TIME
import com.realityexpander.bikingapp.common.Constants.Companion.NOTIFICATION_CHANNEL_ID
import com.realityexpander.bikingapp.common.Constants.Companion.NOTIFICATION_CHANNEL_NAME
import com.realityexpander.bikingapp.common.Constants.Companion.NOTIFICATION_ID
import com.realityexpander.bikingapp.common.SegmentsOfPolyLatLngLines
import com.realityexpander.bikingapp.common.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : LifecycleService() {  // inherit from LifecycleService to use LiveData which needs LifeCycleOwner

    // for notification only, only used in the service
    private val rideTimeElapsedWholeSecondsInMillis = MutableLiveData<Long>()

    private var isFirstTimeServiceStarted = true
    private var isServiceKilled = false

    private var lapTimeInMillis = 0L // time since we started/continued the timer to the time we last paused it
    private var totalRideTimeElapsedInMillis = 0L // total time of all laps of the timer
    private var timeLapStartedInMillis = 0L // the time when we last started the timer
    private var lastWholeSecondInMillis = 0L

    // These are exposed outside the service
    companion object {
        val rideTimeElapsedInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathSegments = MutableLiveData<SegmentsOfPolyLatLngLines>()
    }

    // Base notification builder that contains the settings every notification will have
    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    private lateinit var curNotification: NotificationCompat.Builder

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        curNotification = baseNotificationBuilder
        postInitialValues()

        // We can observe the lifecycle of the service because we inherit from LifecycleService
        isTracking.observe(this) { isTracking ->
            updateNotificationTrackingState(isTracking)
            updateRequestLocationUpdates(isTracking)
        }
    }

    private fun postInitialValues() {
        rideTimeElapsedInMillis.postValue(0L)
        isTracking.postValue(false)
        pathSegments.postValue(mutableListOf())
        rideTimeElapsedWholeSecondsInMillis.postValue(0L)
    }

    // Respond to commands
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstTimeServiceStarted) {
                        startForegroundService() // service should only be started once.
                        isFirstTimeServiceStarted = false
                        isServiceKilled = false
                    } else {
                        startTimerAndTracking()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service")
                    pauseTimerAndTracking()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service.")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun killService() {
        isServiceKilled = true
        isFirstTimeServiceStarted = true

        pauseTimerAndTracking()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    // Activate or deactivate location tracking
    @SuppressLint("MissingPermission") // done with EasyPermissions, but for some reason the compiler doesn't see it
    private fun updateRequestLocationUpdates(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = LOCATION_UPDATE_FASTEST_INTERVAL
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                    maxWaitTime = LOCATION_UPDATE_MAXIMUM_WAIT_TIME
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    // Location Callback: receives location updates and adds the LatLng point to last item of pathSegments list.
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            if(isTracking.value!!) {
                result?.locations?.let { locations ->
                    for(location in locations) {
                        addPathPointToLastPathSegment(location)
                    }
                }
            }
        }
    }

    // Starts the timer for the tracking.
    private fun startTimerAndTracking() {
        addEmptySegment()
        timeLapStartedInMillis = System.currentTimeMillis()

        isTracking.postValue(true)

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time difference between now and time last started/continued
                lapTimeInMillis = System.currentTimeMillis() - timeLapStartedInMillis

                // post the new lapTime
                rideTimeElapsedInMillis.postValue(totalRideTimeElapsedInMillis + lapTimeInMillis)

                // if a new round whole second is reached, we want to update timeRunInSeconds
                // This is to update the notification every second
                if (rideTimeElapsedInMillis.value!! >= lastWholeSecondInMillis + 1000L) {
                    rideTimeElapsedWholeSecondsInMillis.postValue(rideTimeElapsedWholeSecondsInMillis.value!! + 1)
                    lastWholeSecondInMillis += 1000L
                }

                delay(Constants.TIMER_UPDATE_INTERVAL)
            }

            totalRideTimeElapsedInMillis += lapTimeInMillis
        }
    }

    private fun pauseTimerAndTracking() {
        isTracking.postValue(false) // stop the tracking & timer
    }

    private fun addPathPointToLastPathSegment(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)

            pathSegments.value?.apply {
                last().add(pos)
                pathSegments.postValue(this)
            }
        }
    }

    // Add an empty segment to the pathSegments list OR initialize it if empty.
    private fun addEmptySegment() = pathSegments.value?.apply {
            add(mutableListOf())
            pathSegments.postValue(this)
        } ?:
            pathSegments.postValue(mutableListOf(mutableListOf()))

    // Start timer, tracking, notification foreground service and observer for timer updates.
    private fun startForegroundService() {
        Timber.d("Foreground Service started.")

        // Start notification foreground service
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        // should be called "sendForegroundMessage", not "startForeground"... ugh google. You are the worst.
        startForeground(NOTIFICATION_ID, curNotification.build())

        // Start timer and tracking
        startTimerAndTracking()

        // collect updates for notification
        rideTimeElapsedWholeSecondsInMillis.observe(this) { rideTimeInSeconds ->
            if(!isServiceKilled) {
                val notification = curNotification
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(rideTimeInSeconds * 1000L))

                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        }
    }

    // Updates the action buttons of the notification
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Add the action buttons to the notification
        // HACKY but no other way to do this
        curNotification.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotification, ArrayList<NotificationCompat.Action>())
        }

        if(!isServiceKilled) {
            curNotification = baseNotificationBuilder
                .addAction(
                    R.drawable.ic_pause_black_24dp,
                    notificationActionText,
                    pendingIntent
                )
            notificationManager.notify(NOTIFICATION_ID, curNotification.build()) // update the notification
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW  // keep the notification silent
        )
        notificationManager.createNotificationChannel(channel)
    }
}














