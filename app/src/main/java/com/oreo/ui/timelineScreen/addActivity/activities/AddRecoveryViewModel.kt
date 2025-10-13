package com.oreo.ui.timelineScreen.addActivity.activities

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.model.timeline.SupplementOption
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
class AddRecoveryViewModel @Inject constructor(
    private val userRepository: UserRepository,
): BaseViewModel() {

    var date: String? = null

    var startTime: LocalTime ?= null
    var endTime: LocalTime ?= null

    var isDropdownOpen = false
    var selectedOption: SupplementOption ?= null
    val onAddSuccess = MutableLiveData<Event<Boolean>>()

    val recoveryListData = MutableLiveData<ArrayList<SupplementOption>>()
    var isListLoadedFirstTime = true

    var editData : ItemTimelineResponseModel ?= null

    var lunaOption: String ?= null

    fun getRecoveryOptionsList(){
        viewModelScope.launch {
            userRepository.getTimelineOptionIdData("recovery").collect{ resource ->
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
                                        getRecoveryOptionsList()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            it.options?.let {
                                recoveryListData.postValue(it as ArrayList)
                            }
                        }
                    }
                }
            }
        }
    }

    fun logRecovery(editEventFun: () -> Unit) {
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val sTime = startTime?.format(formatter)
            val eTime = endTime?.format(formatter)

            val alcoholObject = JsonObject().apply {
                editData?.id?.let {id ->
                    this.addProperty("id", id)
                }
                this.addProperty("event", "recovery")
                this.addProperty("date", date)
                this.addProperty("start_time", sTime)
                this.addProperty("end_time", eTime)
                this.addProperty("tag", selectedOption?.id)
            }

            val reqData = JsonObject().apply {
                this.add("events", JsonArray().apply {
                    this.add(alcoholObject)
                })
            }

            userRepository.submitLogRecoveryTimelineData(reqData).collect{ resource ->
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
                                        logRecovery(editEventFun)
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

    fun deleteRecoveryItem(deleteEventFun: () -> Unit) {
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
                                        deleteRecoveryItem(deleteEventFun)
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

    fun getRecoveryDates(): Array<String> {
        val dates = mutableListOf<String>()
        val dateToday = LocalDate.now()
        val format = DateTimeFormatter.ofPattern("dd MMM yyyy")
        dates.add(dateToday.format(format).toString())
        dates.add(dateToday.minusDays(1).format(format).toString())
        return dates.reversed().toTypedArray()
    }

}