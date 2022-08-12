package com.realityexpander.bikingapp.ui

import androidx.lifecycle.ViewModel
import com.realityexpander.bikingapp.repositories.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    rideRepository: RideRepository
) : ViewModel() {

    var totalDistance = rideRepository.getTotalDistance()
    var totalTimeInMillis = rideRepository.getTotalTimeInMillis()
    var totalAvgSpeed = rideRepository.getTotalAvgSpeed()
    var totalCaloriesBurned = rideRepository.getTotalCaloriesBurned()

    var ridesSortedByDate = rideRepository.getAllRidesSortedByDate()
}