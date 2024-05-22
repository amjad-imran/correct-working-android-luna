package com.oreo.ui.femalehealth.cycletracker.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.CycleLogDataModel
import com.oreo.data.model.FlowLog
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CycleLogViewModel @Inject constructor(
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {
    fun getCycleLogData(): CycleLogDataModel {
        val childData = CycleLogDataModel()
        val flowListData = ArrayList<FlowLog>()
        val symptomsListData = ArrayList<FlowLog>()
        flowListData.add(FlowLog(image = R.drawable.ic_fmh_light_flow, title = "None"))
        flowListData.add(FlowLog(image = R.drawable.ic_fmh_light_flow, title = "Light"))
        flowListData.add(FlowLog(image = R.drawable.ic_fmh_medium_flow, title = "Medium"))
        flowListData.add(FlowLog(image = R.drawable.ic_fmh_medium_flow, title = "Heavy"))

        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Bloating"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Cramps"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Backache"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Fatigue"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Heavy"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Headache"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Acne"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Craving"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Abdomen pain"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Tender breast"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Constipation"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Back pain"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Mood swing"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Vaginal itching"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Vaginal dryness"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Nausea"))

        childData.flowData = flowListData
        childData.symptomsData = symptomsListData
        return childData
    }

    fun onCalendarDateSelected(selectedDate: String) {
        //calender date set
    }

    private val _logPeriodData = MutableLiveData<Event<Boolean>>()
    val logPeriodData: LiveData<Event<Boolean>>
        get() = _logPeriodData

    fun logPeriod(flow: String?, selectedSymptoms: java.util.ArrayList<String>?) {
        val jsonObject = JsonObject()

        jsonObject.addProperty("period_date", "2024-05-13")
        jsonObject.addProperty("period_cycle", 3)
        jsonObject.addProperty("cycle_length", 28)
        jsonObject.addProperty("flow_type", flow)
        val symptomsArray = JsonArray()
        selectedSymptoms?.forEach {
            symptomsArray.add(it)
        }
        jsonObject.add("symptoms", symptomsArray)
        viewModelScope.launch {
            userActivityRepository.logPeriod(jsonObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        logPeriod(flow, selectedSymptoms)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _logPeriodData.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }
}