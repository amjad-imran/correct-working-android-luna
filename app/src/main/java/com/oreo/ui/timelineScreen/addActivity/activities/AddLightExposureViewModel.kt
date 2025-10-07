package com.oreo.ui.timelineScreen.addActivity.activities

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddLightExposureViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    val defaultMinutes = 30L

    val lightTime = MutableLiveData<LocalTime>(LocalTime.now().minusMinutes(defaultMinutes))
    val lightDuration = MutableLiveData<Long>(defaultMinutes)
    val onAddSuccess = MutableLiveData<Event<Boolean>>()

    var editData : ItemTimelineResponseModel ?= null
    var editDataDuration : Long ?= null

    fun initData(){
        if(editData==null){
            lightTime.postValue(LocalTime.now().minusMinutes(defaultMinutes))
            lightDuration.postValue(defaultMinutes)
        }else{
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val sTime = LocalTime.parse(editData?.startTime, formatter)
            val eTime = LocalTime.parse(editData?.endTime, formatter)

            val durationInMins = Duration.between(sTime, eTime).toMinutes()
            val roundedMinutes = (durationInMins / 5) * 5L

            lightTime.postValue(sTime)
            lightDuration.postValue(roundedMinutes)
            editDataDuration = roundedMinutes
        }
    }

    fun logLightExposure(startTime: LocalTime, duration: Long, editEventFun: () -> Unit) {
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val startTimeStr = startTime.format(formatter)

            val endTime = startTime.plusMinutes(duration)
            val endTimeStr = endTime.format(formatter)

            val lightExpObject = JsonObject().apply {
                editData?.id?.let {id ->
                    this.addProperty("id", id)
                }
                this.addProperty("event", "light-exposure")
                this.addProperty("start_time", startTimeStr)
                this.addProperty("end_time", endTimeStr)
            }

            val reqData = JsonObject().apply {
                this.add("events", JsonArray().apply {
                    this.add(lightExpObject)
                })
            }
            userRepository.submitLogLightExposureTimelineData(reqData).collect{ resource ->
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
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        logLightExposure(startTime, duration, editEventFun)
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            onAddSuccess.postValue(Event(true))

                            editData?.id?.let {id ->
                                editEventFun()
                            }
                        }
                    }
                }
            }
        }
    }

    fun deleteLightExposureItem(deleteEventFun: () -> Unit) {
        viewModelScope.launch {
            userRepository.deleteTimelineItemById(editData?.id?:"").collect{ resource ->
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
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        deleteLightExposureItem(deleteEventFun)
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            onAddSuccess.postValue(Event(true))
                            deleteEventFun()
                        }
                    }
                }
            }

        }
    }

}