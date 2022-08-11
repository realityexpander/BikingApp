package com.realityexpander.bikingapp.repositories

import com.realityexpander.bikingapp.db.Ride
import com.realityexpander.bikingapp.db.RideDao
import javax.inject.Inject

class MainRepository @Inject constructor(
    val rideDao: RideDao
) {
    suspend fun insertRide(ride: Ride) = rideDao.insertRide(ride)

    suspend fun deleteRide(ride: Ride) = rideDao.deleteRide(ride)

    fun getAllRidesSortedByDate() = rideDao.getAllRidesSortedByDate()

    fun getAllRidesSortedByTimeInMillis() = rideDao.getAllRidesSortedByTimeInMillis()

    fun getAllRidesSortedByDistance() = rideDao.getAllRidesSortedByDistance()

    fun getAllRidesSortedByCaloriesBurned() = rideDao.getAllRidesSortedByCaloriesBurned()

    fun getAllRidesSortedByAvgSpeed() = rideDao.getAllRidesSortedByAvgSpeed()

    fun getTotalDistance() = rideDao.getTotalDistance()

    fun getTotalTimeInMillis() = rideDao.getTotalTimeInMillis()

    fun getTotalAvgSpeed() = rideDao.getTotalAvgSpeed()

    fun getTotalCaloriesBurned() = rideDao.getTotalCaloriesBurned()
}