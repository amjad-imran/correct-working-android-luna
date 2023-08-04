package com.oreo.ui.workout.detect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.local.db.CacheResult
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.getParseList
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.repository.abstraction.OreoSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetectWorkoutViewModel
@Inject
constructor(
    private val syncRepository: OreoSyncRepository
) : BaseViewModel() {

    var dayKey: String = ""
    var movementList: List<Int>? = null
    private val _oreoAutoSportData =
        MutableLiveData<Pair<ArrayList<String>, LinkedHashMap<String, ArrayList<OreoAutoSportData>>>>()
    val oreoAutoSportData: LiveData<Pair<ArrayList<String>, LinkedHashMap<String, ArrayList<OreoAutoSportData>>>> =
        _oreoAutoSportData

    private val _dayTimeMovementList = MutableLiveData<List<Int>>()
    val dayTimeMovementList: LiveData<List<Int>> = _dayTimeMovementList


    fun getNotAcceptingData() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getAutoWorkoutData().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        val hm = LinkedHashMap<String, ArrayList<OreoAutoSportData>>()


                        resource.value?.forEach {
                            val date = DateFormats.convertTimestampToDate(
                                it.startTime ,
                                DateFormats.monthDateWithoutYear2
                            )



                            if (hm.containsKey(date)) {
                                val programmeList = hm[date]!!
                                programmeList.add(it)
                                hm[date] = programmeList
                            } else {
                                val programmeList = ArrayList<OreoAutoSportData>()
                                programmeList.add(it)
                                hm[date] = programmeList

                            }
                        }

                        val dateList = ArrayList<String>()
                        hm.forEach {
                            dateList.add(it.key)
                        }


                        dateList.reverse()
                        _oreoAutoSportData.postValue(Pair(dateList, hm))
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

        }
    }

    fun getDayTimeMovement(date:String) {

        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getMovementData(date).collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        _dayTimeMovementList.postValue(resource.value.getParseList())
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

        }
    }

    fun deleteAutoSport(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.deleteAutoWorkoutData(id).collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {


                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

        }
    }

}