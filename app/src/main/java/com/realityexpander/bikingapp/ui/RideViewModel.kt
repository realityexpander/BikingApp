package com.realityexpander.bikingapp.ui

import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.lifecycle.Transformations.switchMap
import com.realityexpander.bikingapp.common.SortType
import com.realityexpander.bikingapp.common.SortType.*
import com.realityexpander.bikingapp.db.Ride
import com.realityexpander.bikingapp.repositories.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class RideViewModel @Inject constructor(
    val rideRepository: RideRepository,   // Hilt automatically finds this dependency (rideDao)
) : ViewModel() {

    private val ridesSortedByDate = rideRepository.getAllRidesSortedByDate()
    private val ridesSortedByDistance = rideRepository.getAllRidesSortedByDistance()
    private val ridesSortedByTimeInMillis = rideRepository.getAllRidesSortedByTimeInMillis()
    private val ridesSortedByAvgSpeed = rideRepository.getAllRidesSortedByAvgSpeed()
    private val ridesSortedByCaloriesBurned = rideRepository.getAllRidesSortedByCaloriesBurned()

    val sortType = MutableLiveData(SortType.DATE)
    val rides: LiveData<List<Ride>> = switchMap(sortType) { sortType ->
        when (sortType) {
            DATE            -> ridesSortedByDate
            BIKING_TIME     -> ridesSortedByTimeInMillis
            DISTANCE        -> ridesSortedByDistance
            AVG_SPEED       -> ridesSortedByAvgSpeed
            CALORIES_BURNED -> ridesSortedByCaloriesBurned
            else            -> ridesSortedByDate
        }
    }

    fun sortRides(type: SortType) {
        sortType.value = type
    }

    fun insertRide(ride: Ride) = viewModelScope.launch {
        rideRepository.insertRide(ride)
    }

    fun deleteRide(ride: Ride) = viewModelScope.launch {
        rideRepository.deleteRide(ride)
    }
}
