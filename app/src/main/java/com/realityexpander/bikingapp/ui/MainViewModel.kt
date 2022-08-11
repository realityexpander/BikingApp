package com.realityexpander.bikingapp.ui

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.bikingapp.db.Ride
import com.realityexpander.bikingapp.common.SortType
import com.realityexpander.bikingapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository   // Hilt automatically finds this dependency (rideDao)
) : ViewModel() {

    private val ridesSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val ridesSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val ridesSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()
    private val ridesSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    private val ridesSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()

    val rides = MediatorLiveData<List<Ride>>()

    var sortType = SortType.DATE

    // Sets correct run list in MediatorLiveData depending on sortType
    init {
        rides.addSource(ridesSortedByDate) { result ->
            Timber.d("RUNS SORTED BY DATE")
            if(sortType == SortType.DATE) {
                result?.let { rides.value = it }
            }
        }
        rides.addSource(ridesSortedByDistance) { result ->
            if(sortType == SortType.DISTANCE) {
                Timber.d("RUNS SORTED BY DISTANCE")
                result?.let { rides.value = it }
            }
        }
        rides.addSource(ridesSortedByTimeInMillis) { result ->
            if(sortType == SortType.BIKING_TIME) {
                Timber.d("RUNS SORTED BY BIKING TIME")
                result?.let { rides.value = it }
            }
        }
        rides.addSource(ridesSortedByAvgSpeed) { result ->
            if(sortType == SortType.AVG_SPEED) {
                Timber.d("RUNS SORTED BY AVG SPEED")
                result?.let { rides.value = it }
            }
        }
        rides.addSource(ridesSortedByCaloriesBurned) { result ->
            if(sortType == SortType.CALORIES_BURNED) {
                Timber.d("RUNS SORTED BY CALORIES BURNED")
                result?.let { rides.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType) {
        SortType.DATE ->
            ridesSortedByDate.value?.let { rides.value = it }
        SortType.DISTANCE ->
            ridesSortedByDistance.value?.let { rides.value = it }
        SortType.BIKING_TIME ->
            ridesSortedByTimeInMillis.value?.let { rides.value = it }
        SortType.AVG_SPEED ->
            ridesSortedByAvgSpeed.value?.let { rides.value = it }
        SortType.CALORIES_BURNED ->
            ridesSortedByCaloriesBurned.value?.let { rides.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(ride: Ride) = viewModelScope.launch {
        mainRepository.insertRun(ride)
    }

    fun deleteRun(ride: Ride) = viewModelScope.launch {
        mainRepository.deleteRun(ride)
    }
}