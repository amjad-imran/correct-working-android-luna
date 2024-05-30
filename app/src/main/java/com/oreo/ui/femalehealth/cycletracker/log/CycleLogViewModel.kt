package com.oreo.ui.femalehealth.cycletracker.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.PeriodCycleHistory
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.femalehealth.cycletracker.DayState
import com.oreo.ui.femalehealth.cycletracker.PeriodPos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class CycleLogViewModel @Inject constructor(
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

    var selectedDate: LocalDate = LocalDate.now()
    val todayDate = LocalDate.now()

    private val _openDayLogBottomSheet = MutableLiveData<Event<Boolean>?>()
    val openDayLogBottomSheet: LiveData<Event<Boolean>?> get() = _openDayLogBottomSheet

    private val _cycleHistoryData = MutableLiveData<PeriodCycleHistory?>()
    val cycleHistoryData: LiveData<PeriodCycleHistory?> get() = _cycleHistoryData

    private val _openLogBottomSheet = MutableLiveData<Event<Boolean>?>()
    val openLogBottomSheet: LiveData<Event<Boolean>?> get() = _openLogBottomSheet

    val healthDataDateList = HashMap<LocalDate, DayState>()

    val notifyDateChanged = MutableLiveData<Event<List<LocalDate>>>()

    private val _logPeriodData = MutableLiveData<Event<Boolean>>()
    val logPeriodData: LiveData<Event<Boolean>>
        get() = _logPeriodData

    var lastDateInteraction: LocalDate? = null



    fun setOpenDayLogBottomSheet(status: Boolean) {
        _openDayLogBottomSheet.postValue(Event(status))
    }

    fun setOpenLogBottomSheet(status: Boolean) {
        _openLogBottomSheet.postValue(Event(status))
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

    private fun generateHealthData(cycleData: PeriodCycleHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val mainPeriodLength = 5
            val mainCycleLength = 28

            cycleData.cycleHistory?.forEach { data ->

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

            val currentPeriodStart = LocalDate.parse(cycleData.userDefault?.firstPeriodDate)

            val preProcessDataTill = currentPeriodStart.plusMonths(12)

            val nextPeriodDate =
                currentPeriodStart.plusDays(mainPeriodLength.toLong())

            var current = nextPeriodDate
            while (current <= preProcessDataTill) {

                val currentDay = getCurrentCycleDay(
                    LocalDate.parse(cycleData.userDefault?.firstPeriodDate),
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
            _cycleHistoryData.postValue(cycleData)
            setLoading(false)
        }
    }


    /**
     * return Pair(DayState, isDateSelected)
     */
    //TODO optimize - pre process data

    fun getCurrentState(date: LocalDate): Pair<DayState, Boolean> {
        val isDateSelected = date == selectedDate

        val history = cycleHistoryData.value
        if (history?.cycleHistory.isNullOrEmpty()) {
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

    fun getFirstPeriodDate(): LocalDate {
        val periodDate = cycleHistoryData.value?.userDefault?.firstPeriodDate ?: "2024-03-01"
        return LocalDate.parse(periodDate)
    }

    fun getDefaultPeriodLength(): Long {
        return cycleHistoryData.value?.userDefault?.periodLength?.toLong() ?: 5L
    }

    /**
     * Start date, period array
     */
    var periodList = HashMap<LocalDate, ArrayList<String>>()


    fun onCalendarDateClicked(selectedDate: LocalDate) {
        val (state, selected) = getCurrentState(selectedDate)
        val prevDay = selectedDate.minusDays(1)
        val nextDay = selectedDate.plusDays(1)
        val isPrevPeriodDay = healthDataDateList[prevDay] is DayState.Period
        val isNextPeriodDay = healthDataDateList[nextDay] is DayState.Period

        if (isPrevPeriodDay && isNextPeriodDay) {
            return
        }

        val daysToNotify = mutableListOf<LocalDate>()

        if (state !is DayState.Period) {
            val lastPeriodDate = hasPeriodInLastNDays(selectedDate, 7)
            if (lastPeriodDate == null) {
                val periodLength = getDefaultPeriodLength()
                val periodEndDate = selectedDate.plusDays(periodLength)
                var loopDate = selectedDate
                while (loopDate < periodEndDate) {
                    healthDataDateList[loopDate] = DayState.Period(PeriodPos.SINGLE)
                    daysToNotify.add(loopDate)
                    loopDate = loopDate.plusDays(1)
                }

            } else {
                var loopDate = lastPeriodDate
                while (loopDate != selectedDate) {
                    healthDataDateList[loopDate!!] = DayState.Period(PeriodPos.SINGLE)
                    daysToNotify.add(loopDate)
                    loopDate = loopDate.plusDays(1)
                }
                healthDataDateList[selectedDate] = DayState.Period(PeriodPos.SINGLE)
                daysToNotify.add(selectedDate)
            }


        } else {
            healthDataDateList[selectedDate] = DayState.Default
            daysToNotify.add(selectedDate)
        }
        lastDateInteraction = selectedDate

        notifyDateChanged.value = Event(daysToNotify)
    }

    /**
     * Returns last period date if period in last N days
     */
    private fun hasPeriodInLastNDays(selectedDate: LocalDate, days: Long): LocalDate? {
        val lastNDate = selectedDate.minusDays(days)

        var date = selectedDate
        var hasPeriodInLastNDays = false
        while (date != lastNDate) {
            if (healthDataDateList[date] is DayState.Period) {
                hasPeriodInLastNDays = true
                break
            }

            date = date.minusDays(1)
        }
        if (hasPeriodInLastNDays) {
            return date
        } else {
            return null
        }
    }

    fun savePeriodLog() {
        viewModelScope.launch(Dispatchers.IO) {

            val sortedData =
                healthDataDateList.filter { (it.value is DayState.Period) && (it.key <= todayDate) }
                    .toSortedMap()

            val requestObject = JsonObject()
            val topLevelJsonArray = JsonArray()
            var datesArray = JsonArray()
            val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")


            var lastValue: Map.Entry<LocalDate, DayState>? = null

            sortedData.forEach {
                val currentKey = it.key

                if (lastValue == null || ChronoUnit.DAYS.between(
                        lastValue!!.key,
                        currentKey
                    ) != 1L
                ) {
                    lastValue = null
                    if (datesArray.isEmpty.not()) {
                        topLevelJsonArray.add(datesArray)
                    }
                    datesArray = JsonArray()

                }

                datesArray.add(currentKey.format(pattern))
                lastValue = it

            }

            if (datesArray.isEmpty.not()) {
                topLevelJsonArray.add(datesArray)
            }
            requestObject.add("dates", topLevelJsonArray)

            userActivityRepository.logPeriod(requestObject).collect { resource ->
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
                                        savePeriodLog()
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

    fun getLastInteractedRange(): Pair<String, String> {
        return Pair("2024-05-20", "2024-05-25")
    }

}