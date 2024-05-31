package com.oreo.ui.femalehealth.cycletracker.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
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
    private val femaleHealthRepository: FemaleHealthRepository
) : BaseViewModel() {
    var cycleTrackerInfo: FemaleCycleTrackInfoModel? = null
    var lastSelectedGoal: String? = null
    var lastSelectedPeriodLength: String? = null
    var lastSelectedCycleLength: String? = null
    var isGoalUpdated: Boolean = false
    var isCycleLengthUpdated: Boolean = false
    var isPeriodLengthUpdated: Boolean = false
    private val _cycleTrackInfo = MutableLiveData<Event<Boolean>>()

    fun updateCycleTrackerInfo() {
        val jsonObject = JsonObject()
        if (isGoalUpdated)
            jsonObject.addProperty("goal", lastSelectedGoal)
        if (isPeriodLengthUpdated)
            jsonObject.addProperty("period_length", lastSelectedPeriodLength)
        if (isCycleLengthUpdated)
            jsonObject.addProperty("cycle_length", lastSelectedCycleLength)
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

}