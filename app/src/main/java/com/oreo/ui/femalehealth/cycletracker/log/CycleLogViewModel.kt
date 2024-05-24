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
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.data.model.FlowLog
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.femalehealth.cycletracker.DayState
import com.oreo.ui.femalehealth.cycletracker.PeriodPos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class CycleLogViewModel @Inject constructor(
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

    var selectedDate: LocalDate = LocalDate.now()
    val todayDate = LocalDate.now()

    private val _cycleHistoryData = MutableLiveData<List<FMHCycleHistoryDataModel>?>()
    val cycleHistoryData: LiveData<List<FMHCycleHistoryDataModel>?> get() = _cycleHistoryData


    init {
        getCycleHistoryData()
    }

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

    fun getCycleHistoryData() {
        viewModelScope.launch {
            userActivityRepository.getPeriodCycleHistory().collect { resource ->
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
                                        getCycleHistoryData()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _cycleHistoryData.postValue(it)
                        }
                    }
                }
            }
        }
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

    /**
     * return Pair(DayState, isDateSelected)
     */
    //TODO optimize - pre process data
    fun getCurrentState(date: LocalDate): Pair<DayState, Boolean> {
        val isDateSelected = date == selectedDate

        val history = cycleHistoryData.value
        if (history.isNullOrEmpty()) {
            return Pair(DayState.Default, isDateSelected)
        }

        if (date > todayDate) {
            val data = history.first()

            val periodLength = data.periodLength ?: 0
            val cycleLength = data.cycleLength ?: 0

            val currentDay = getCurrentCycleDay(LocalDate.parse(data.periodDate), cycleLength, date)

            if (currentDay == 1) {
                return Pair(DayState.Period(PeriodPos.START), isDateSelected)
            } else if (currentDay == periodLength) {
                return Pair(DayState.Period(PeriodPos.END), isDateSelected)
            }

            if (currentDay <= periodLength) {
                return Pair(DayState.Period(PeriodPos.CENTER), isDateSelected)
            }

            val ovDay = cycleLength - 13

            if (currentDay == ovDay) {
                return Pair(DayState.OvulationDay, isDateSelected)
            }
            if (currentDay in (ovDay - 5)..(ovDay + 1)) {
                return Pair(DayState.Fertile, isDateSelected)
            }

            return Pair(DayState.Default, isDateSelected)
        }

        var returnValue: Pair<DayState, Boolean>? = null

        history.forEach {
            val periodDateStart = LocalDate.parse(it.periodDate)
            val periodLength = it.periodLength ?: 0

            val periodDateEnd = if (periodLength == 0) {
                periodDateStart
            } else {
                periodDateStart.plusDays((periodLength - 1).toLong())
            }
            if (date == periodDateStart) {
                returnValue =
                    Pair(DayState.Period(PeriodPos.START), isDateSelected)
                return@forEach
            } else if (date == periodDateEnd) {
                returnValue =
                    Pair(DayState.Period(PeriodPos.END), isDateSelected)
                return@forEach
            }

            if (date in periodDateStart..periodDateEnd) {
                returnValue =
                    Pair(DayState.Period(PeriodPos.CENTER), isDateSelected)
                return@forEach
            }

            val ovDay = LocalDate.parse(it.ovulationStartDate)
            if (ovDay == date) {
                returnValue = Pair(
                    DayState.OvulationDay,
                    isDateSelected
                )
                return@forEach
            }

            try {
                val fWindow = it.fertileWindow?.split("/")

                val fertileDateStart = LocalDate.parse(fWindow?.get(0))
                val fertileDateEnd = LocalDate.parse(fWindow?.get(1))

                if (date in fertileDateStart..fertileDateEnd) {
                    returnValue = Pair(
                        DayState.Fertile,
                        isDateSelected
                    )
                    return@forEach
                }

            } catch (exp: Exception) {
            }


        }
        return if (returnValue == null) {
            Pair(DayState.Default, isDateSelected)
        } else {
            returnValue as Pair<DayState, Boolean>
        }
    }

    private fun getCurrentCycleDay(
        periodDate: LocalDate,
        cycleLength: Int,
        currentDate: LocalDate
    ): Int {
        val daysSinceLastPeriod = ChronoUnit.DAYS.between(periodDate, currentDate).toInt()
        return (daysSinceLastPeriod % cycleLength) + 1
    }
}