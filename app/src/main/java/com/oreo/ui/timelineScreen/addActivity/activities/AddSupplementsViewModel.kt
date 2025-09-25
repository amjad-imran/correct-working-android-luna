package com.oreo.ui.timelineScreen.addActivity.activities

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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

) : BaseViewModel(){

    var selectedDate: String ?= null
    var supplementTime: LocalTime = LocalTime.now()

    val onAddSuccess = MutableLiveData<Event<Boolean>>()

    val supplementsList = MutableLiveData<ArrayList<String>>()

    fun getSupplementsList(){
        viewModelScope.launch {
            val supplements = listOf(
                "Select Supplement", // This is the hint
                "Calcium", "Magnesium", "Ashwagandha", "Creatine", "Melatonin",
                "Vitamin B12", "Vitamin D3", "Probiotic", "Zinc", "Omega 3",
                "Whey Protein", "Electrolytes"
            )

            supplementsList.postValue(ArrayList(supplements))
        }
    }

    fun logSupplements() {
        viewModelScope.launch {
            val time = supplementTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

            // TODO : Change json req obj and API
            val alcoholObject = JsonObject().apply {
                this.addProperty("event", "alcohol")
                this.addProperty("date", selectedDate)
                this.addProperty("time", time)
            }

            val reqData = JsonObject().apply {
                this.add("events", JsonArray().apply {
                    this.add(alcoholObject)
                })
            }
            onAddSuccess.postValue(Event(true))
            return@launch

            /*userRepository.submitLogMealTimelineData(reqData).collect{ resource ->
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
            }*/
        }
    }

    fun getSupplementsDates(): Array<String> {
        val dates = mutableListOf<String>()
        val dateToday = LocalDate.now()
        val format = DateTimeFormatter.ofPattern("dd MMM yyyy")
        dates.add(dateToday.format(format).toString())
        dates.add(dateToday.minusDays(1).format(format).toString())
        dates.add(dateToday.minusDays(2).format(format).toString())
        dates.add(dateToday.minusDays(3).format(format).toString())
        return dates.reversed().toTypedArray()

    }

}