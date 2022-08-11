package com.realityexpander.bikingapp.di

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.realityexpander.bikingapp.R
import com.realityexpander.bikingapp.common.Constants
import com.realityexpander.bikingapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

/**
 * ServiceModule, provides dependencies for the TrackingService
 */
@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    @SuppressLint("VisibleForTests") // SuppressLint is used to suppress warnings about the visibility of the method
    fun providesFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = FusedLocationProviderClient(context)

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder =
        NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_bike)
            .setContentTitle("Bike Tracking App")
            .setContentText("00:00:00")
            .setContentIntent(pendingIntent)  // really should be called "setPendingIntent"

    @ServiceScoped
    @Provides
    fun provideActivityPendingIntent(
        @ApplicationContext context: Context
    ): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else
                PendingIntent.FLAG_UPDATE_CURRENT
        )
}