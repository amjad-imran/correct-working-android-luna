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
class AddSupplementsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel(){

    var selectedDate: String ?= null
    var supplementTime: LocalTime = LocalTime.now()

    val onAddSuccess = MutableLiveData<Event<Boolean>>()

    val supplementsList = MutableLiveData<ArrayList<SupplementOption>>()
    var selectedOption: SupplementOption ?= null
    var isDropdownOpen = false

    var editData : ItemTimelineResponseModel ?= null
    var isListLoadedFirstTime = true

    fun getSupplementsList(){
        viewModelScope.launch {

            userRepository.getAddSupplementsListData().collect { resource ->

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
                                        getSupplementsList()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.options?.let {
                            supplementsList.postValue(ArrayList(it))
                        }
                    }
                }

            }
        }
    }

    fun logSupplements() {
        viewModelScope.launch {
            val time = supplementTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

            val supplementsObject = JsonObject().apply {
                this.addProperty("event", "supplements")
                this.addProperty("date", selectedDate)
                this.addProperty("time", time)
                this.addProperty("tag", selectedOption?.id)
            }

            val reqData = JsonObject().apply {
                this.add("events", JsonArray().apply {
                    this.add(supplementsObject)
                })
            }

            userRepository.submitLogSupplementsTimelineData(reqData).collect{ resource ->
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

    fun deleteSupplementItem() {
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
                                        deleteSupplementItem()
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

    fun getSupplementsDates(): Array<String> {
        val dates = mutableListOf<String>()
        val dateToday = LocalDate.now()
        val format = DateTimeFormatter.ofPattern("dd MMM yyyy")
        dates.add(dateToday.format(format).toString())
        dates.add(dateToday.minusDays(1).format(format).toString())
        return dates.reversed().toTypedArray()

    }

}