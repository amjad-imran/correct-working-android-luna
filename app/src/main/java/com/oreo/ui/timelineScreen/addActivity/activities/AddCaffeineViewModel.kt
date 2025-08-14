package com.oreo.ui.timelineScreen.addActivity.activities

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddCaffeineViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    val caffeineTime = MutableLiveData<LocalTime>(LocalTime.now())
    val caffeineValue = MutableLiveData<Int>(30)
    val onAddSuccess = MutableLiveData<Event<Boolean>>()

    fun logCaffeineValue(localTime: LocalTime, quantity: Int) {
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val startTime = localTime.format(formatter)

            val caffeineObject = JsonObject().apply {
                this.addProperty("event", "caffeine")
                this.addProperty("value", quantity)
                this.addProperty("start_time", startTime)
                this.addProperty("unit", "mg")
            }

            val reqData = JsonObject().apply {
                this.add("events", JsonArray().apply {
                    this.add(caffeineObject)
                })
            }
            userRepository.submitLogCaffeineTimelineData(reqData).collect{ resource ->
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

                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            onAddSuccess.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }

}