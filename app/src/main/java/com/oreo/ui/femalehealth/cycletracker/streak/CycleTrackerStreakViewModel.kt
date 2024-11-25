package com.oreo.ui.femalehealth.cycletracker.streak

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.repository.abstraction.FemaleHealthRepository
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
    private val femaleHealthRepository: FemaleHealthRepository,
    val resourcesProvider: ResourcesProvider,
) : BaseViewModel() {

    lateinit var cycleData: FMHCycleHistoryDataModel

    fun getSymptomsData(): ArrayList<String> {
        return arrayListOf("Test1", "Test2", "Test3", "Test4")
    }

    val todayDate = LocalDate.now()
    var notifyDateChange = MutableLiveData<Event<LocalDate>>()

    var selectedDate: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())

    private val _femaleHealthData = MutableLiveData<FemaleHealthUserInfoModel?>()
    val femaleHealthData: LiveData<FemaleHealthUserInfoModel?> get() = _femaleHealthData

    private val _symptomList = MutableLiveData<ArrayList<Pair<String, String>>>()
    val symptomList: LiveData<ArrayList<Pair<String, String>>> get() = _symptomList


    fun getDataForDate(date: String) {
        viewModelScope.launch {
            femaleHealthRepository.getFemaleHealthUserInfo(date).collect { resource ->
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

                            val symList = ArrayList<Pair<String, String>>()
                            it?.symptom?.flow?.let {
                                val title = resourcesProvider.getString(
                                    R.string.text_flow_value,
                                    it.symptomName ?: ""
                                )
                                symList.add(Pair(it.icon ?: "", title))
                            }
                            it?.symptom?.symptoms?.forEach {
                                symList.add(Pair(it.icon ?: "", it.symptomName ?: ""))
                            }

                            _symptomList.postValue(symList)
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

    private fun getCurrentCycleDay(
        periodDate: LocalDate,
        cycleLength: Int,
        currentDate: LocalDate
    ): Int {
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
            val cycleEnd = cycleData.getCycleEnd()
            if (newDate > cycleEnd) {
                updateSelectedDate(cycleEnd)
            } else {
                updateSelectedDate(newDate)
            }
        } else {
            val days = abs(ChronoUnit.DAYS.between(date, selectedDate.value))
            val diff = days % 7

            val newDate = date.plusDays(diff)
            val cycleStart = cycleData.getCycleStart()
            if (newDate < cycleStart) {
                updateSelectedDate(cycleStart)
            } else {
                updateSelectedDate(newDate)
            }
        }
    }

    /**
     * return Pair(DayState, isDateSelected)
     */
    fun getCurrentState(date: LocalDate): Pair<DayState, Boolean> {
        val isDateSelected = date == selectedDate.value

        val periodDateStart = cycleData.getCycleStart()
        val periodLength = cycleData.periodLength ?: 0

        try {
            val ovDay = LocalDate.parse(cycleData.ovulationStartDate)
            if (ovDay == date) {
                return Pair(
                    DayState.OvulationDay,
                    isDateSelected
                )
            }

            val fWindow = cycleData.fertileWindow?.split("/")

            val fertileDateStart = LocalDate.parse(fWindow?.get(0))
            val fertileDateEnd = LocalDate.parse(fWindow?.get(1))

            if (date in fertileDateStart..fertileDateEnd) {
                return Pair(
                    DayState.Fertile,
                    isDateSelected
                )
            }

        } catch (exp: Exception) {
            return Pair(DayState.Default, isDateSelected)
        }

        val periodDateEnd = if (periodLength == 0) {
            periodDateStart
        } else {
            periodDateStart.plusDays((periodLength - 1).toLong())
        }

        if (date in periodDateStart..periodDateEnd) {
            return Pair(DayState.Period(PeriodPos.CENTER), isDateSelected)
        }




        return Pair(DayState.Default, isDateSelected)
    }

    fun isCycleLengthNormal(cycleLength: Int): Boolean {
        return cycleLength in 21..35
    }

    fun isPeriodLengthNormal(cycleLength: Int): Boolean {
        return cycleLength in 2..7
    }
}