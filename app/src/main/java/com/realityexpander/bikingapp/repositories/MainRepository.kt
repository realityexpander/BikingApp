package com.realityexpander.bikingapp.repositories

import com.realityexpander.bikingapp.db.Ride
import com.realityexpander.bikingapp.db.RideDao
import javax.inject.Inject

class MainRepository @Inject constructor(
    val rideDao: RideDao
) {
    suspend fun insertRun(ride: Ride) = rideDao.insertRun(ride)

    suspend fun deleteRun(ride: Ride) = rideDao.deleteRun(ride)

    fun getAllRunsSortedByDate() = rideDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByTimeInMillis() = rideDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByDistance() = rideDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByCaloriesBurned() = rideDao.getAllRunsSortedByCaloriesBurned()

    fun getAllRunsSortedByAvgSpeed() = rideDao.getAllRunsSortedByAvgSpeed()

    fun getTotalDistance() = rideDao.getTotalDistance()

    fun getTotalTimeInMillis() = rideDao.getTotalTimeInMillis()

    fun getTotalAvgSpeed() = rideDao.getTotalAvgSpeed()

    fun getTotalCaloriesBurned() = rideDao.getTotalCaloriesBurned()
}