package com.oreo.ui.femalehealth.cycletracker.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.femaleh.FemaleCycleTrackInfoModel
import com.oreo.data.repository.abstraction.FemaleHealthRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CycleTrackerSettingViewModel @Inject constructor(
    private val femaleHealthRepository: FemaleHealthRepository,
    private val localDataStore: DataStoredInterface,
) : BaseViewModel() {

    val cycleTrackerEnabled = MutableLiveData<Boolean>()

    var cycleTrackerInfo: FemaleCycleTrackInfoModel? = null
    var lastSelectedGoal: String? = null
    var lastSelectedPeriodLength: String? = null
    var lastSelectedCycleLength: String? = null
    var isGoalUpdated: Boolean = false
    var isCycleLengthUpdated: Boolean = false
    var isPeriodLengthUpdated: Boolean = false
    private val _cycleTrackInfo = MutableLiveData<Event<Boolean>>()


    private fun convertToInt(value: String?): Int? {
        return try {
            value?.toIntOrNull()
        } catch (exp: Exception) {
            null
        }
    }

    fun updateCycleTrackerInfo() {
        val jsonObject = JsonObject()
        if (isGoalUpdated)
            jsonObject.addProperty("goal", lastSelectedGoal)
        if (isPeriodLengthUpdated)
            jsonObject.addProperty("period_length", convertToInt(lastSelectedPeriodLength))
        if (isCycleLengthUpdated)
            jsonObject.addProperty("cycle_length", convertToInt(lastSelectedCycleLength))
        viewModelScope.launch {
            cycleTrackerInfo?.id?.let {
                femaleHealthRepository.updateCycleTrackerInfo(jsonObject, it).collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }

                        is Resource.Loading -> {
                            setLoading(resource.loading)
                        }

                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                    object :
                                        BinaryActionCallback {
                                        override fun yes() {
                                            updateCycleTrackerInfo()
                                        }

                                        override fun no() {}
                                    }
                            })
                        }

                        is Resource.Success -> {
                            resource.data?.data.let {
                                _cycleTrackInfo.postValue(Event(true))
                            }
                        }
                    }
                }
            }
        }
    }

    fun updatePeriodToggle(isChecked: Boolean) {
        viewModelScope.launch {
            val requestObj = JsonObject().apply {
                this.addProperty("status", isChecked)
            }
            femaleHealthRepository.updateCycleTrackerToggle(requestObj).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                        cycleTrackerEnabled.postValue(isChecked.not())
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        cycleTrackerEnabled.postValue(isChecked.not())
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object :
                                    BinaryActionCallback {
                                    override fun yes() {
                                        updatePeriodToggle(isChecked)
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            localDataStore.setFemaleHealthStatus(isChecked)
                            cycleTrackerEnabled.postValue(isChecked)
                        }
                    }
                }
            }
        }


    }

    fun getPeriodTrackerStatus() {
        cycleTrackerEnabled.postValue(
            localDataStore.getFemaleHealthStatus()
        )
    }

}