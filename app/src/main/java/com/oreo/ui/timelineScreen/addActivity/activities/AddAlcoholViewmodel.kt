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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddAlcoholViewmodel @Inject constructor(
    private val userRepository: UserRepository,
):
    BaseViewModel() {

    var alcoholId: Int ?= null

    var selectedDate: String ?= null
    var alcoholTime: LocalTime = LocalTime.now()

    val onAddSuccess = MutableLiveData<Event<Boolean>>()
    var editData : ItemTimelineResponseModel ?= null

    fun getAlcoholIdFromServer(){
        viewModelScope.launch {
            userRepository.getTimelineOptionIdData("alcohol").collect{ resource ->
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
                            it.options?.first()?.id?.let {
                                alcoholId = it
                            }
                        }
                    }
                }
            }
        }
    }

    fun logAlcohol() {
        viewModelScope.launch {
            val time = alcoholTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

            // TODO : Change json req obj and API
            val alcoholObject = JsonObject().apply {
                this.addProperty("date", selectedDate)
                this.addProperty("time", time)
                this.addProperty("tag", alcoholId)
            }

            val reqData = JsonObject().apply {
                this.add("events", JsonArray().apply {
                    this.add(alcoholObject)
                })
            }

            userRepository.submitLogAlcoholTimelineData(reqData).collect{ resource ->
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

    fun deleteAlcoholItem() {
        viewModelScope.launch {
            if(editData?.id == null){
                return@launch
            }
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
                                        deleteAlcoholItem()
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

    fun getAlcoholDates(): Array<String> {
        val dates = mutableListOf<String>()
        val dateToday = LocalDate.now()
        val format = DateTimeFormatter.ofPattern("dd MMM yyyy")
        dates.add(dateToday.format(format).toString())
        dates.add(dateToday.minusDays(1).format(format).toString())
        return dates.reversed().toTypedArray()

    }
}