package com.oreo.ui.googlefit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.GoogleFitDataDb
import com.noisefit_commans.models.SleepDataGoogleFit
import com.noisefit_commans.models.WorkoutGoogleFit
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.abstaction.GoogleFitDataSource
import com.oreo.data.model.GoogleFitDataDisplayModel
import com.oreo.data.model.GoogleFitDataType
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "GoogleFitDataViewModel"
@HiltViewModel
class GoogleFitDataViewModel
@Inject
constructor(
    private val googleFitDataSource: GoogleFitDataSource,
    private val userActivityRepository: OreoUserActivityRepository,
    private val offlineDataMapper: OfflineDataMapper,
) : BaseViewModel() {
    val success = ArrayList<GoogleFitDataDisplayModel>()
    val fail = ArrayList<GoogleFitDataDisplayModel>()
    var unSyncedDataList = MutableLiveData<List<GoogleFitDataDisplayModel>>()
        private set

    var dataSyncingComplete = MutableLiveData<Event<Boolean>>()
        private set

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {

            val data = googleFitDataSource.getUnSyncedData()
            unSyncedDataList.postValue(convertData(data))
        }

    }

    private fun convertData(data: List<GoogleFitDataDb>): List<GoogleFitDataDisplayModel> {

        val result = ArrayList<GoogleFitDataDisplayModel>()

        data.forEach {

            //workout, sleep, height, weight, body_fat

            val type = if (it.type.equals("workout", true)) {
                GoogleFitDataType.WORKOUT
            } else if (it.type.equals("sleep", true)) {
                val duration = it.endTime - it.startTime
                if (duration < (3 * 60 * 60)) {
                    GoogleFitDataType.NAP
                } else {
                    GoogleFitDataType.SLEEP
                }
            } else {
                null
            }

            if (type != null) {
                /*val data = if(type==GoogleFitDataType.WORKOUT){
                    Gson().fromJson<WorkoutGoogleFit>(it.data?:"")
                }else{
                    Gson().fromJson<SleepDataGoogleFit>(it.data?:"")
                }*/
                result.add(
                    GoogleFitDataDisplayModel(
                        id = it.id,
                        type = type,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        duration = it.endTime - it.startTime,
                        rawData = it.data ?: "",
                    )
                )
            }
        }

        return result
    }

    fun getSyncedMessage(): String {
        var hasSleep = false
        var hasWorkout = false
        var hasNap = false
        var hasBodyMeasurement = false

        success.forEach {
            when(it.type){
                GoogleFitDataType.SLEEP -> {
                    hasSleep = true
                }
                GoogleFitDataType.NAP -> {
                    hasNap = true
                }
                GoogleFitDataType.WORKOUT -> {
                    hasWorkout = true
                }
                GoogleFitDataType.BODY_MEASUREMENTS -> {
                    hasBodyMeasurement = true
                }
            }
        }


        return "Great! Your have successfully synced {message pending}"

    }


    fun sendDataToServer(selectedItems: List<GoogleFitDataDisplayModel>) {
        viewModelScope.launch {
            LOGS.d("sendDataToServer API start")
            selectedItems.map { googleFitDataDisplayModel ->
                LOGS.d("sendDataToServer API hit ")

                when (googleFitDataDisplayModel.type) {
                    GoogleFitDataType.NAP -> {

                        val data = offlineDataMapper.convertGFManualNap(googleFitDataDisplayModel)
                        userActivityRepository.addManualNap(
                            data.first, data.second
                        ).collect { resource ->
                            when (resource) {
                                is Resource.GenericError -> {
                                    fail.add(googleFitDataDisplayModel)
                                    LOGS.d("sendDataToServer API GenericError")
                                }

                                is Resource.Loading -> {
                                    setLoading(resource.loading)

                                }

                                is Resource.NetworkError -> {
                                    fail.add(googleFitDataDisplayModel)
                                    LOGS.d("sendDataToServer API NetworkError ")
                                }

                                is Resource.Success -> {
                                    resource.data?.data?.let {
                                        LOGS.d("sendDataToServer API success $it")
                                        success.add(googleFitDataDisplayModel)
                                    }
                                }
                            }
                        }
                    }

                    GoogleFitDataType.SLEEP -> {
                        val data = offlineDataMapper.convertGFManualSleep(
                            Gson().fromJson<SleepDataGoogleFit>(
                                googleFitDataDisplayModel.rawData ?: ""
                            )
                        )
                        userActivityRepository.addManualSleep(data.first, data.second)
                            .collect { resource1 ->
                                when (resource1) {
                                    is Resource.GenericError -> {
                                        fail.add(googleFitDataDisplayModel)
                                        LOGS.d("$TAG workout session api error")
                                    }

                                    is Resource.Loading -> {

                                    }

                                    is Resource.NetworkError -> {
                                        fail.add(googleFitDataDisplayModel)
                                    }

                                    is Resource.Success -> {
                                        success.add(googleFitDataDisplayModel)
                                        LOGS.d("$TAG workout session api success")

                                    }
                                }

                            }
                    }

                    GoogleFitDataType.WORKOUT -> {
                        userActivityRepository.addGFitWorkout(
                            offlineDataMapper.convert1GFWorkoutIntoJsonArray(
                                Gson().fromJson<WorkoutGoogleFit>(
                                    googleFitDataDisplayModel.rawData ?: ""
                                )
                            )
                        ).collect { resource1 ->
                            when (resource1) {
                                is Resource.GenericError -> {
                                    fail.add(googleFitDataDisplayModel)
                                    LOGS.d("$TAG workout session api error")
                                }

                                is Resource.Loading -> {

                                }

                                is Resource.NetworkError -> {
                                    fail.add(googleFitDataDisplayModel)
                                }

                                is Resource.Success -> {
                                    success.add(googleFitDataDisplayModel)
                                    LOGS.d("$TAG workout session api success")

                                }
                            }

                        }
                    }

                    GoogleFitDataType.BODY_MEASUREMENTS -> {


                    }
                }


                LOGS.d("sendDataToServer API response ")
            }

            viewModelScope.launch(Dispatchers.IO) {
                success.map {
                    googleFitDataSource.markDataSynced(it.id)
                }
            }

            dataSyncingComplete.postValue(Event(true))

            LOGS.d("sendDataToServer API response end")
        }
    }


}
