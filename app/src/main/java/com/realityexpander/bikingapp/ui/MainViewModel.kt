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

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()

    val rides = MediatorLiveData<List<Ride>>()

    var sortType = SortType.DATE

    /**
     * Posts the correct run list in the LiveData
     */
    init {
        rides.addSource(runsSortedByDate) { result ->
            Timber.d("RUNS SORTED BY DATE")
            if(sortType == SortType.DATE) {
                result?.let { rides.value = it }
            }
        }
        rides.addSource(runsSortedByDistance) { result ->
            if(sortType == SortType.DISTANCE) {
                result?.let { rides.value = it }
            }
        }
        rides.addSource(runsSortedByTimeInMillis) { result ->
            if(sortType == SortType.BIKING_TIME) {
                result?.let { rides.value = it }
            }
        }
        rides.addSource(runsSortedByAvgSpeed) { result ->
            if(sortType == SortType.AVG_SPEED) {
                result?.let { rides.value = it }
            }
        }
        rides.addSource(runsSortedByCaloriesBurned) { result ->
            if(sortType == SortType.CALORIES_BURNED) {
                result?.let { rides.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType) {
        SortType.DATE -> runsSortedByDate.value?.let { rides.value = it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { rides.value = it }
        SortType.BIKING_TIME -> runsSortedByTimeInMillis.value?.let { rides.value = it }
        SortType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { rides.value = it }
        SortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { rides.value = it }
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