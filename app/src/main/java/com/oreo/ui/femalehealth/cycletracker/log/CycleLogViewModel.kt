package com.oreo.ui.femalehealth.cycletracker.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS

import com.oreo.data.model.FMHCycleHistoryDataModel

import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.femalehealth.cycletracker.DayState
import com.oreo.ui.femalehealth.cycletracker.PeriodPos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    val healthDataDateList = HashMap<LocalDate, DayState>()


    init {
        getCycleHistoryData()
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
                            generateHealthData(it)
                        }
                    }
                }
            }
        }
    }

    private fun generateHealthData(it: List<FMHCycleHistoryDataModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            val mainPeriodLength = 5
            val mainCycleLength = 28

            it.forEach { data ->

                val periodLength = data.periodLength ?: 0
                val cycleLength = data.cycleLength ?: 0

                val periodStart = LocalDate.parse(data.periodDate)
                val periodEnd = if (periodLength == 0) {
                    periodStart
                } else {
                    periodStart.plusDays((periodLength - 1).toLong())
                }

                var loopDate = periodStart
                while (loopDate <= periodEnd) {

                    val state = if (periodEnd == periodStart) {
                        PeriodPos.SINGLE
                    } else {
                        if (loopDate == periodStart) {
                            PeriodPos.START
                        } else if (loopDate == periodEnd) {
                            PeriodPos.END
                        } else {
                            PeriodPos.CENTER
                        }
                    }

                    healthDataDateList[loopDate] = DayState.Period(state)

                    loopDate = loopDate.plusDays(1)
                }


                try {
                    val fWindow = data.fertileWindow?.split("/")

                    val fertileDateStart = LocalDate.parse(fWindow?.get(0))
                    val fertileDateEnd = LocalDate.parse(fWindow?.get(1))

                    var loopDateFertile = fertileDateStart
                    while (loopDateFertile <= fertileDateEnd) {
                        healthDataDateList[loopDateFertile] = DayState.Fertile
                        loopDateFertile = loopDateFertile.plusDays(1)
                    }

                    val ovDate = LocalDate.parse(data.ovulationStartDate)
                    healthDataDateList[ovDate] = DayState.OvulationDay


                } catch (exp: Exception) {
                }
            }

            val currentPeriodStart = LocalDate.parse(it.first().periodDate)

            val preProcessDataTill = currentPeriodStart.plusMonths(12)

            val nextPeriodDate =
                currentPeriodStart.plusDays(mainPeriodLength.toLong())

            var current = nextPeriodDate
            val data = it.first()
            while (current <= preProcessDataTill) {

                val currentDay = getCurrentCycleDay(
                    LocalDate.parse(data.periodDate),
                    mainCycleLength,
                    current
                )

                if (currentDay == 1) {
                    healthDataDateList[current] = DayState.Period(PeriodPos.START)
                    current = current.plusDays(1)
                    continue
                } else if (currentDay == mainPeriodLength) {
                    healthDataDateList[current] = DayState.Period(PeriodPos.END)
                    current = current.plusDays(1)
                    continue
                }

                if (currentDay <= mainPeriodLength) {
                    healthDataDateList[current] = DayState.Period(PeriodPos.CENTER)
                    current = current.plusDays(1)
                    continue
                }

                val ovDay = (mainCycleLength - 13)

                if (currentDay == ovDay) {
                    healthDataDateList[current] = DayState.OvulationDay
                    current = current.plusDays(1)
                    continue
                }

                if (currentDay in (ovDay - 5)..(ovDay + 1)) {
                    healthDataDateList[current] = DayState.Fertile
                    current = current.plusDays(1)
                    continue
                }
                current = current.plusDays(1)

            }
            _cycleHistoryData.postValue(it)
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

        val returnVal = healthDataDateList[date]

        return if (returnVal == null) {
            Pair(DayState.Default, isDateSelected)
        } else {
            Pair(returnVal, isDateSelected)
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