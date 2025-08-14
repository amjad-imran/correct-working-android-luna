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
class AddMealViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {


    val mealTime = MutableLiveData<LocalTime>(LocalTime.now())
    val onAddSuccess = MutableLiveData<Event<Boolean>>()

    fun logMeal(localTime: LocalTime) {
        viewModelScope.launch {
            val time = localTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

            val mealObject = JsonObject().apply {
                this.addProperty("event", "meal")
                this.addProperty("start_time", time)
            }

            val reqData = JsonObject().apply {
                this.add("events", JsonArray().apply {
                    this.add(mealObject)
                })
            }
            userRepository.submitLogMealTimelineData(reqData).collect{ resource ->
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