package com.realityexpander.bikingapp.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RideDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: Ride)

    @Delete
    suspend fun deleteRide(ride: Ride)

    @Query("SELECT * FROM ride_table ORDER BY timestamp DESC")
    fun getAllRidesSortedByDate(): LiveData<List<Ride>>

    @Query("SELECT * FROM ride_table ORDER BY timeInMillis DESC")
    fun getAllRidesSortedByTimeInMillis(): LiveData<List<Ride>>

    @Query("SELECT * FROM ride_table ORDER BY caloriesBurned DESC")
    fun getAllRidesSortedByCaloriesBurned(): LiveData<List<Ride>>

    @Query("SELECT * FROM ride_table ORDER BY distanceInMeters DESC")
    fun getAllRidesSortedByDistance(): LiveData<List<Ride>>

    @Query("SELECT * FROM ride_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRidesSortedByAvgSpeed(): LiveData<List<Ride>>

    @Query("SELECT SUM(timeInMillis) FROM ride_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(distanceInMeters) FROM ride_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT AVG(avgSpeedInKMH) FROM ride_table")
    fun getTotalAvgSpeed(): LiveData<Float>

    @Query("SELECT SUM(caloriesBurned) FROM ride_table")
    fun getTotalCaloriesBurned(): LiveData<Long>

}