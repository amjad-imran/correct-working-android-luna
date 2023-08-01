package com.oreo.ui.workout.detect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.local.db.CacheResult
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.repository.abstraction.OreoSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DetectWorkoutViewModel
@Inject
constructor(
    private val syncRepository: OreoSyncRepository
) : BaseViewModel() {

    private val _oreoAutoSportData =
        MutableLiveData<Pair<ArrayList<String>, HashMap<String, ArrayList<OreoAutoSportData>>>>()
    val oreoAutoSportData: LiveData<Pair<ArrayList<String>, HashMap<String, ArrayList<OreoAutoSportData>>>> =
        _oreoAutoSportData


    fun getNotAcceptingData() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getAutoWorkoutData().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        val hm = HashMap<String, ArrayList<OreoAutoSportData>>()

//                        val dummyList = ArrayList<OreoAutoSportData>()
//                        dummyList.addAll(resource.value!!)
//                        dummyList.addAll(resource.value!!)
//                        dummyList.addAll(resource.value!!)

                        resource.value?.forEach {
                            val date = DateFormats.convertTimestampToDate(
                                it.startTime ,
                                DateFormats.monthDateWithoutYear2
                            )
//                            val date = Random.nextInt(0,100).toString()

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


                        _oreoAutoSportData.postValue(Pair(dateList, hm))
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

        }
    }

}