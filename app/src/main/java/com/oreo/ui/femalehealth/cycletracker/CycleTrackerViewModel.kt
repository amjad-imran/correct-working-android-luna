package com.oreo.ui.femalehealth.cycletracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.joda.time.Days
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class CycleTrackerViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository,
) : BaseViewModel() {

    var selectedDate: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())
    var notifyDateChange = MutableLiveData<Event<LocalDate>>()

    private val _femaleHealthData = MutableLiveData<FemaleHealthUserInfoModel>()
    val femaleHealthData: LiveData<FemaleHealthUserInfoModel> get() = _femaleHealthData

    private val _cycleHistoryData = MutableLiveData<List<FMHCycleHistoryDataModel>?>()
    val cycleHistoryData: LiveData<List<FMHCycleHistoryDataModel>?> get() = _cycleHistoryData

    init {

        getCycleHistoryData()
        //getDataForDate(viewModel.selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }


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


    fun isCycleLengthNormal(cycleLength: Int): Boolean {
        return cycleLength in 21..35
    }

    fun isPeriodLengthNormal(cycleLength: Int): Boolean {
        return cycleLength in 2..7
    }

    fun getPregnancyText(text: String?): String {
        return if (text.equals("high", true)) {
            "High chance of pregnancy"
        } else if (text.equals("low", true)) {
            "Low chance of pregnancy"
        } else if (text.equals("fertile", true)) {
            "Your body is at it’s most fertile today"
        } else {
            ""
        }
    }

    /**
     * Returns Pair(Phase string, phase color)
     */
    fun getCurrentPhaseText(
        fertileWindowList: List<String>?, periodDate: String?, currentDate: String
    ): Pair<String, Int>? {
        if (fertileWindowList == null) return null
        if (fertileWindowList.size != 2) return null
        if (periodDate.isNullOrEmpty()) return null

        val localCurrentDate = LocalDate.parse(currentDate)
        val fertileStart = LocalDate.parse(fertileWindowList.first())

        return if (localCurrentDate.isBefore(fertileStart)) {
            Pair("Follicular phase", R.color.color_follicular)
        } else {
            Pair("Luteal phase", R.color.color_luteal)
        }
    }

    fun calculateDaysLeft(dateString: String, selectedDate: String): Long {
        val targetDate = LocalDate.parse(dateString)
        val today = LocalDate.parse(selectedDate)
        return ChronoUnit.DAYS.between(today, targetDate)
    }

    fun updateSelectedDate(date: LocalDate) {
        val old = selectedDate.value
        notifyDateChange.value = Event(old)

        selectedDate.postValue(date)
    }

    /**
     * return Pair(DayState, isDateSelected)
     */
    fun getCurrentState(date: LocalDate): Pair<DayState, Boolean> {
        val isDateSelected = date == selectedDate.value

        val history = cycleHistoryData.value
        if (history.isNullOrEmpty()) {
            return Pair(DayState.DEFAULT, isDateSelected)
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
                    Pair(DayState.PERIOD, isDateSelected)
                return@forEach
            }

            val ovDay = LocalDate.parse(it.ovulationStartDate)
            if (ovDay == date) {
                returnValue = Pair(
                    DayState.OVULATION_DAY,
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
                        DayState.FERTILE,
                        isDateSelected
                    )
                    return@forEach
                }

            } catch (exp: Exception) {
            }


        }
        return if (returnValue == null) {
            Pair(DayState.DEFAULT, isDateSelected)
        } else {
            returnValue as Pair<DayState, Boolean>
        }
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

}

enum class DayState {
    FERTILE, OVULATION_DAY, PERIOD, DEFAULT
}