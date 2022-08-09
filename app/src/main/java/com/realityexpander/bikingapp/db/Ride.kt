package com.realityexpander.bikingapp.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ride_table")
data class Ride(
    var img: Bitmap? = null,
    var timestamp: Long = 0L,     // date of the ride in milliseconds
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0,   // Length of the ride in milliseconds
    var caloriesBurned: Int = 0
) {
    @PrimaryKey(autoGenerate = true)  // room handles generation and uniqueness of primary keys
    var id: Int? = null
}