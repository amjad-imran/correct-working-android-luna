package com.oreo.ui.femalehealth.cycletracker.streak

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.femalehealth.cycletracker.DayState
import com.oreo.ui.femalehealth.cycletracker.PeriodPos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class CycleTrackerStreakViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {
    var id: String? = null
    fun getSymptomsData(): ArrayList<String> {
        return arrayListOf("Test1", "Test2", "Test3", "Test4")
    }
    val todayDate = LocalDate.now()
    var notifyDateChange = MutableLiveData<Event<LocalDate>>()

    var selectedDate: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())

    private val _cycleStreakData = MutableLiveData<List<FMHCycleHistoryDataModel>?>()
    val cycleStreakData: LiveData<List<FMHCycleHistoryDataModel>?> get() = _cycleStreakData
    fun getStreakInfoData() {
        viewModelScope.launch {
            //todo api will update later, once provided by backend
            userActivityRepository.getCycleStreakInfo().collect { resource ->
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
                                        getStreakInfoData()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _cycleStreakData.postValue(it)
                        }
                    }
                }
            }
        }
    }

    private val _femaleHealthData = MutableLiveData<FemaleHealthUserInfoModel>()
    val femaleHealthData: LiveData<FemaleHealthUserInfoModel> get() = _femaleHealthData
    fun getDataForDate(date: String) {
        viewModelScope.launch {
            userActivityRepository.getFemaleHealthUserInfo(date).collect { resource ->
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
                                        getDataForDate(date)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            _femaleHealthData.postValue(it)
                        }
                    }
                }
            }

        }
    }
    fun updateSelectedDate(date: LocalDate) {
        val old = selectedDate.value

        selectedDate.value = date
        notifyDateChange.value = Event(old)
    }
    private fun getCurrentCycleDay(periodDate: LocalDate, cycleLength: Int, currentDate: LocalDate): Int {
        val daysSinceLastPeriod = ChronoUnit.DAYS.between(periodDate, currentDate).toInt()
        return (daysSinceLastPeriod % cycleLength) + 1
    }
    fun onWeekScrolled(date: LocalDate) {
        if (date > selectedDate.value) {
            val days = abs(ChronoUnit.DAYS.between(date, selectedDate.value))
            val diff = days % 7

            val newDate = if (diff == 0L) {
                date
            } else {
                date.plusDays(7 - diff)
            }
            updateSelectedDate(newDate)
        } else {
            val days = abs(ChronoUnit.DAYS.between(date, selectedDate.value))
            val diff = days % 7

            val newDate = date.plusDays(diff)
            updateSelectedDate(newDate)

        }


    }

    /**
     * return Pair(DayState, isDateSelected)
     */
    fun getCurrentState(date: LocalDate): Pair<DayState, Boolean> {
        val isDateSelected = date == selectedDate.value

        val history = cycleStreakData.value
        if (history.isNullOrEmpty()) {
            return Pair(DayState.Default, isDateSelected)
        }

        if (date > todayDate) {
            val data = history.first()

            val periodLength = data.periodLength ?: 0
            val cycleLength = data.cycleLength ?: 0

            val currentDay = getCurrentCycleDay(LocalDate.parse(data.periodDate), cycleLength, date)

            if (currentDay <= periodLength) {
                return Pair(DayState.Period(PeriodPos.START), isDateSelected)
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
}