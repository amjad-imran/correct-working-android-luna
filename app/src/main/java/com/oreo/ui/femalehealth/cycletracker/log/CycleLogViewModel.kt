package com.oreo.ui.femalehealth.cycletracker.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.dataConverter.FemaleHealthDataConvertor
import com.oreo.data.dataConverter.FemaleHealthGeneratorResult
import com.oreo.data.model.PeriodCycleHistory
import com.oreo.data.repository.abstraction.FemaleHealthRepository
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
    val femaleHealthRepository: FemaleHealthRepository,
    val femaleHealthDataConvertor: FemaleHealthDataConvertor,
    val sessionManager: SessionManager
) : BaseViewModel() {

    var selectedDate: LocalDate = LocalDate.now()
    val todayDate = LocalDate.now()
    var shouldGenerateFutureData = true

    private val _openDayLogBottomSheet = MutableLiveData<Event<Boolean>?>()
    val openDayLogBottomSheet: LiveData<Event<Boolean>?> get() = _openDayLogBottomSheet

    private val _cycleHistoryData = MutableLiveData<PeriodCycleHistory?>()
    val cycleHistoryData: LiveData<PeriodCycleHistory?> get() = _cycleHistoryData

    private val _openLogBottomSheet = MutableLiveData<Event<Boolean>?>()
    val openLogBottomSheet: LiveData<Event<Boolean>?> get() = _openLogBottomSheet

    var healthDataDateList = HashMap<LocalDate, DayState>()

    val notifyDateChanged = MutableLiveData<Event<List<LocalDate>>>()

    private val _logPeriodData = MutableLiveData<Event<Boolean>>()
    val logPeriodData: LiveData<Event<Boolean>>
        get() = _logPeriodData

    private val _navigateToBack = MutableLiveData<Event<Boolean>>()
    val navigateToBack: LiveData<Event<Boolean>> get() = _navigateToBack

    var lastDateInteraction: List<LocalDate>? = null


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
            femaleHealthRepository.getPeriodCycleHistory().collect { resource ->
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

                            if (it.cycleHistory.isNullOrEmpty()) {
                                _navigateToBack.postValue(Event(true))
                                return@collect
                            }

                            generateHealthData(it)
                        }
                    }
                }
            }
        }
    }

    private fun generateHealthData(
        cycleData: PeriodCycleHistory
    ) {

        viewModelScope.launch {

            femaleHealthDataConvertor.convertHealthData(cycleData, shouldGenerateFutureData)
                .collect { resource ->

                    when (resource) {
                        is FemaleHealthGeneratorResult.Loading -> {
                            setLoading(resource.loading)
                        }

                        is FemaleHealthGeneratorResult.Success -> {
                            healthDataDateList.clear()
                            healthDataDateList = resource.value
                            _cycleHistoryData.postValue(cycleData)
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


    fun removePeriodAfterToday(date: LocalDate) {

        var loopDate = date
        var isNextPeriodDay: Boolean

        val daysToNotify = mutableListOf<LocalDate>()

        do {
            daysToNotify.add(loopDate)
            healthDataDateList[loopDate] = DayState.Default
            daysInteractedWith[loopDate] = false
            val nextDay = loopDate.plusDays(1)
            isNextPeriodDay = healthDataDateList[nextDay] is DayState.Period
            loopDate = nextDay
        } while (isNextPeriodDay)

        notifyDateChanged.value = Event(daysToNotify)

    }

    /**
     * Stores local date and a boolean->true if added, false if in removed list
     */
    private var daysInteractedWith = HashMap<LocalDate, Boolean>()

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

        var isRemoved = false
        if (state !is DayState.Period) {
            val lastPeriodDate = hasPeriodInLastNDays(selectedDate, 7)
            if (lastPeriodDate == null) {
                val periodLength = getDefaultPeriodLength()
                val periodEndDate = selectedDate.plusDays(periodLength)
                var loopDate = selectedDate
                while (loopDate < periodEndDate) {
                    healthDataDateList[loopDate] = DayState.Period(PeriodPos.SINGLE)
                    daysToNotify.add(loopDate)
                    daysInteractedWith[loopDate] = true
                    loopDate = loopDate.plusDays(1)
                }

            } else {
                var loopDate = lastPeriodDate!!
                while (loopDate < selectedDate) {
                    healthDataDateList[loopDate] = DayState.Period(PeriodPos.SINGLE)
                    daysToNotify.add(loopDate)
                    daysInteractedWith[loopDate] = true
                    loopDate = loopDate.plusDays(1)
                }
                healthDataDateList[selectedDate] = DayState.Period(PeriodPos.SINGLE)
                daysInteractedWith[selectedDate] = true
                daysToNotify.add(selectedDate)
            }

        } else {
            healthDataDateList[selectedDate] = DayState.Default
            daysToNotify.add(selectedDate)
            daysInteractedWith[selectedDate] = false
            isRemoved = true
        }

        if (lastDateInteraction == null) {
            lastDateInteraction = getPeriodRange(selectedDate, isRemoved)
        } else {
            val range = getPeriodRange(selectedDate, isRemoved)
            if (!range.isNullOrEmpty()) {
                lastDateInteraction = getPeriodRange(selectedDate, isRemoved)
            }
        }
        notifyDateChanged.value = Event(daysToNotify)
    }

    private fun getPeriodRange(selectedDate: LocalDate, isRemoved: Boolean): List<LocalDate>? {
        val sortedData = healthDataDateList.filter {
            it.value is DayState.Period
        }.keys.sorted()

        var index = sortedData.indexOf(selectedDate)

        //Check Right
        if (index == -1) {
            val nextDay = selectedDate.plusDays(1)
            index = sortedData.indexOf(nextDay)
        }

        //Check Left
        if (index == -1) {
            val nextDay = selectedDate.minusDays(1)
            index = sortedData.indexOf(nextDay)
        }


        if (index == -1) {
            return null
        }

        var startIndex = index
        while (startIndex > 0 && sortedData[startIndex].minusDays(1) == sortedData[startIndex - 1]) {
            startIndex--
        }

        var endIndex = index
        while (endIndex < sortedData.size - 1 && sortedData[endIndex].plusDays(1) == sortedData[endIndex + 1]) {
            endIndex++
        }
        return sortedData.subList(startIndex, endIndex + 1)
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

            val requestObject = JsonObject()
            val datesObject = JsonObject()
            val addArray = JsonArray()
            val removeArray = JsonArray()

            val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            daysInteractedWith.forEach {
                if (it.value) {
                    addArray.add(it.key.format(pattern))
                } else {
                    removeArray.add(it.key.format(pattern))
                }
            }

            datesObject.add("add_dates", addArray)
            datesObject.add("remove_dates", removeArray)
            requestObject.add("dates", datesObject)


            femaleHealthRepository.logPeriod(requestObject).collect { resource ->
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
        if (lastDateInteraction.isNullOrEmpty()) {
            return Pair(todayDate.toString(), todayDate.toString())
        } else {
            return Pair(
                lastDateInteraction?.first().toString(),
                lastDateInteraction?.last().toString()
            )
        }
    }

    fun getCalendarStart(): LocalDate {
        val periodDate = cycleHistoryData.value?.userDefault?.calendarStart ?: run {
            cycleHistoryData.value?.userDefault?.firstPeriodDate ?: "2024-03-01"
        }
        return LocalDate.parse(periodDate)
    }

    fun getCurrentCyclePeriodDate(): String {
        return cycleHistoryData.value?.cycleHistory?.firstOrNull()?.periodDate ?: ""
    }


}