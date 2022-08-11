package com.realityexpander.bikingapp.ui

import androidx.lifecycle.*
import androidx.lifecycle.Transformations.switchMap
import com.realityexpander.bikingapp.common.SortType
import com.realityexpander.bikingapp.common.SortType.*
import com.realityexpander.bikingapp.db.Ride
import com.realityexpander.bikingapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository,   // Hilt automatically finds this dependency (rideDao)
) : ViewModel() {

    private val ridesSortedByDate = mainRepository.getAllRidesSortedByDate()
    private val ridesSortedByDistance = mainRepository.getAllRidesSortedByDistance()
    private val ridesSortedByTimeInMillis = mainRepository.getAllRidesSortedByTimeInMillis()
    private val ridesSortedByAvgSpeed = mainRepository.getAllRidesSortedByAvgSpeed()
    private val ridesSortedByCaloriesBurned = mainRepository.getAllRidesSortedByCaloriesBurned()

//    val rides = MediatorLiveData<List<Ride>>() // Allows us to combine multiple LiveData sources into one LiveData
    //val rides = MutableLiveData<List<Ride>>()

//    var sortType = DATE

    val sortType = MutableLiveData(SortType.DATE)
//    val rides2: MutableLiveData<List<Ride>> by switchMap(sortType2) {
//        when (it) {
//            DATE -> ridesSortedByDate
//            DISTANCE -> ridesSortedByDistance
//            TIME -> ridesSortedByTimeInMillis
//            AVG_SPEED -> ridesSortedByAvgSpeed
//            CALORIES_BURNED -> ridesSortedByCaloriesBurned
//        }
//    }
    val rides: LiveData<List<Ride>> = Transformations.switchMap(sortType) { sortType ->
        when (sortType) {
            DATE -> ridesSortedByDate
            BIKING_TIME -> ridesSortedByTimeInMillis
            DISTANCE -> ridesSortedByDistance
            AVG_SPEED -> ridesSortedByAvgSpeed
            CALORIES_BURNED -> ridesSortedByCaloriesBurned
            else -> ridesSortedByDate
        }
    }

    init {
        // Sets correct ride list emitted by MediatorLiveData depending on sortType.
        // Only emits ride list for the selected sorted type.

//        rides.addSource(ridesSortedByDate) { result ->
//            //if(sortType == SortType.DATE) {
//                Timber.d("RIDES SORTED BY DATE")
//                result?.let { rides.value = it }
//            //}
//        }
//
//        rides.addSource(ridesSortedByDistance) { result ->
//            //if(sortType == SortType.DISTANCE) {
//                Timber.d("RIDES SORTED BY DISTANCE")
//                result?.let { rides.value = it }
//            //}
//        }
//        rides.addSource(ridesSortedByTimeInMillis) { result ->
//            //if(sortType == SortType.BIKING_TIME) {
//                Timber.d("RIDES SORTED BY BIKING TIME")
//                result?.let { rides.value = it }
//            //}
//        }
//        rides.addSource(ridesSortedByAvgSpeed) { result ->
//            //if(sortType == SortType.AVG_SPEED) {
//                Timber.d("RIDES SORTED BY AVG SPEED")
//                result?.let { rides.value = it }
//            //}
//        }
//        rides.addSource(ridesSortedByCaloriesBurned) { result ->
//            //if(sortType == SortType.CALORIES_BURNED) {
//                Timber.d("RIDES SORTED BY CALORIES BURNED")
//                result?.let { rides.value = it }
//            //}
//        }

//        ridesSortedByDate.observeOnce() { result ->
//            result?.let {
//                rides.value = result
//            }
//        }

    }

    fun sortRides(type: SortType) {
        sortType.value = type
    }


//    // Respond to spinner selection change for the sort type.
//    fun sortRidesOld(sortType: SortType) = when (sortType) {
//        DATE ->
////            ridesSortedByDate.value?.let {
////                rides.value = it
////            }
//            ridesSortedByDate.observeOnce() { result ->
//                result?.let {
//                    rides.value = result
//                }
//            }
//
//        SortType.DISTANCE ->
////            ridesSortedByDistance.value?.let {
////                rides.value = it
////            }
//            ridesSortedByDistance.observeOnce() { result ->
//                result?.let {
//                    rides.value = result
//                }
//            }
//        SortType.BIKING_TIME ->
////            ridesSortedByTimeInMillis.value?.let { rides.value = it }
//            ridesSortedByTimeInMillis.observeOnce() { result ->
//                result?.let {
//                    rides.value = result
//                }
//            }
//        SortType.AVG_SPEED ->
////            ridesSortedByAvgSpeed.value?.let { rides.value = it }
//            ridesSortedByTimeInMillis.observeOnce() { result ->
//                result?.let {
//                    rides.value = result
//                }
//            }
//        SortType.CALORIES_BURNED ->
////            ridesSortedByCaloriesBurned.value?.let { rides.value = it }
//            ridesSortedByCaloriesBurned.observeOnce() { result ->
//                result?.let {
//                    rides.value = result
//                }
//            }
//    }.also {
//        this.sortType = sortType
//    }

    fun insertRide(ride: Ride) = viewModelScope.launch {
        mainRepository.insertRide(ride)
    }

    fun deleteRide(ride: Ride) = viewModelScope.launch {
        mainRepository.deleteRide(ride)
    }
}

fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
    observeForever(object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}