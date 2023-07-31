package com.noisefit.ui.dashboard.graphs.hr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.history.HrBreakup
import com.noisefit_commans.data.model.history.HrHistoryResponse
import com.noisefit_commans.models.HeartRate
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class HeartRateDetailsViewModel
@Inject
constructor(
    private val localDataStore: DataStoredInterface,
    private val userActivityRepository: UserActivityRepository,
    private val syncRepository: SyncRepository,
    val sessionManager: SessionManager
) : BaseViewModel() {

    init {
        getHeartRateHistory()
    }

    private var dailyHistoryResponse: HrHistoryResponse? = null
    private var weeklyHistoryResponse: HrHistoryResponse? = null
    private var yearlyHistoryResponse: HrHistoryResponse? = null
    private var monthlyHistoryResponse: HrHistoryResponse? = null

    private var todayHrBreakup: HrBreakup? = null
    private val _graphInterval = MutableLiveData<GraphInterval>()
    val graphInterval: LiveData<GraphInterval> = _graphInterval

    private val _hrHistoryResponse = MutableLiveData<HrHistoryResponse>()
    val hrHistoryResponse: LiveData<HrHistoryResponse> = _hrHistoryResponse

    var selectedStartDate: String? = null
    var selectedEndDate: String? = null

    val mTodaysDate = DateFormats.getTodaysDateString(10)


    private fun handleHrHistoryResponse(heartList: List<HeartRate>?): HrBreakup {
        val date = DateFormats.getTodaysDateString(10)
        if (heartList.isNullOrEmpty()) {
            return HrBreakup(
                date = date,
                min = 0,
                max = 0,
                avg = 0,
                hourlyBreakUp = ArrayList()
            )
        }

        val minimum = heartList.minOf { it2 -> it2.averageHeartRate }
        val maximum = heartList.maxOf { it2 -> it2.averageHeartRate }
        val average = heartList.map { it2 -> it2.averageHeartRate }.average().roundToInt()


        val hmHeartData = HashMap<Int, ArrayList<Int>>()
        heartList.forEach { heartRate ->
            val timeIn24HoursFormat = DateFormats.formatTimeInto24HoursValue(heartRate.time)
            if (timeIn24HoursFormat.isNotEmpty() && heartRate.averageHeartRate != 0) {
                val timeInInt = timeIn24HoursFormat.toInt()
                if (hmHeartData.containsKey(timeInInt)) {
                    val heartValueList = hmHeartData[timeInInt]!!
                    heartValueList.add(heartRate.averageHeartRate)
                    hmHeartData[timeInInt] = heartValueList
                } else {
                    val heartValueList = ArrayList<Int>()
                    heartValueList.add(heartRate.averageHeartRate)
                    hmHeartData[timeInInt] = heartValueList
                }

            }

        }

        val hourlyBreakupList = ArrayList<HrBreakup>()
        for (time in 0..23) {

            var avg = 0
            var min = 0
            var max = 0
            if (hmHeartData.containsKey(time)) {
                avg = hmHeartData[time]?.average()?.roundToInt() ?: 0
                min = hmHeartData[time]?.minOf { it2 -> it2 } ?: 0
                max = hmHeartData[time]?.maxOf { it2 -> it2 } ?: 0
            }
            hourlyBreakupList.add(
                HrBreakup(
                    hour_of_the_day = time,
                    date = date,
                    avg = avg,
                    max = max,
                    hourlyBreakUp = null,
                    min = min
                )
            )

        }
        return HrBreakup(
            min = minimum,
            max = maximum,
            avg = average,
            date = date,
            hourlyBreakUp = hourlyBreakupList
        )
    }

    private fun getHeartRateHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getTodayHeartRate().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        todayHrBreakup = handleHrHistoryResponse(resource.value)
                        _graphInterval.postValue(GraphInterval.DAY)
                    }
                    is CacheResult.GenericError -> {
                        sendMessage(resource.errorMessage)
                    }
                }

            }

        }
    }

    fun getHrData() {
        viewModelScope.launch {
            /*when (_graphInterval.value) {
                GraphInterval.DAY -> {
                    dailyHistoryResponse?.let {
                        setTodayDataInList(it)
                        return@launch
                    }
                }
                GraphInterval.WEEK -> {
                    weeklyHistoryResponse?.let {
                        setTodayDataInList(it)
                        return@launch
                    }
                }
                GraphInterval.MONTH -> {
                    monthlyHistoryResponse?.let {
                        setTodayDataInList(it)
                        return@launch
                    }
                }
                GraphInterval.YEAR -> {
                    yearlyHistoryResponse?.let {
                        setTodayDataInList(it)
                        return@launch
                    }
                }
                else -> {
                    throw NullPointerException("invalid graph Interval value")
                }
            }*/

            userActivityRepository.getHrHistory(
                getGraphTypeText(),
                selectedStartDate,
                selectedEndDate
            ).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getHrData()
                                }

                                override fun no() {

                                }
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            setTodayDataInList(it)
                        }/* ?: getConfig()*/
                    }
                }
            }
        }

    }

    private fun setTodayDataInList(
        hrHistoryResponse: HrHistoryResponse
    ) {
        when (_graphInterval.value) {
            GraphInterval.DAY -> {
                dailyHistoryResponse = hrHistoryResponse

                val updateTodaysData = if (selectedEndDate.isNullOrEmpty()) {
                    true
                } else {
                    mTodaysDate.equals(selectedEndDate, true)
                }

                if (updateTodaysData) {
                    (hrHistoryResponse.heart_rates as? ArrayList)?.removeLast()
                    todayHrBreakup?.let { (hrHistoryResponse.heart_rates as ArrayList).add(it) }
                }

            }
            GraphInterval.WEEK -> {
                weeklyHistoryResponse = hrHistoryResponse
            }
            GraphInterval.MONTH -> {
                monthlyHistoryResponse = hrHistoryResponse
            }
            GraphInterval.YEAR -> {
                yearlyHistoryResponse = hrHistoryResponse
            }
            else -> {
                throw NullPointerException("invalid online server graph Interval value")
            }
        }

        _hrHistoryResponse.value = (hrHistoryResponse)

    }

    private fun getGraphTypeText(): String {
        return when (_graphInterval.value) {
            GraphInterval.DAY -> "daily"
            GraphInterval.WEEK -> "weekly"
            GraphInterval.MONTH -> "monthly"
            GraphInterval.YEAR -> "yearly"
            else -> ""
        }
    }


    fun onDayClicked() {
        if (_graphInterval.value == GraphInterval.DAY) {
            return
        }
        _graphInterval.value = GraphInterval.DAY
    }

    fun onWeekClicked() {
        if (_graphInterval.value == GraphInterval.WEEK) {
            return
        }
        _graphInterval.value = GraphInterval.WEEK
    }

    fun onMonthClicked() {
        if (_graphInterval.value == GraphInterval.MONTH) {
            return
        }
        _graphInterval.value = GraphInterval.MONTH
    }

    fun onYearClicked() {
        if (_graphInterval.value == GraphInterval.YEAR) {
            return
        }
        _graphInterval.value = GraphInterval.YEAR
    }


    fun getYear(): String {
        return if (selectedEndDate.isNullOrEmpty()) {
            DateFormats.getTodaysDateString(12)
        } else {
            DateFormats.getFormattedYear(selectedEndDate)
        }
    }

    fun getCurrentMonth(data: List<HrBreakup>): String {
        return if (data.isNullOrEmpty() || data.first().date.isNullOrEmpty()) {
            DateFormats.getTodaysDateString(11)
        } else {
            DateFormats.formatDateTime(
                data[0].date,
                DateFormats.dateFormat3,
                DateFormats.dateFormat4
            )
        }
    }

    fun getWeeklyMonth(data: List<HrBreakup>): String {
        return if (data.isNullOrEmpty()) {
            ""
        } else {
            return "${DateFormats.formatWeeklyDateMonth(data.first().date)} - ${
                DateFormats.formatWeeklyDateMonthWithYear(
                    data.last().date
                )
            }"
        }
    }


    fun getRestingHrRating(hrValue: Int): HRRanges {
        val gender = localDataStore.getUser()?.userInfo?.gender ?: "male"
        if (gender.equals("female", true)) {
            return when (hrValue) {
                in 1 until 49 -> HRRanges.LOW
                in 50 until 61 -> HRRanges.EXCELLENT
                in 62 until 73 -> HRRanges.GOOD
                in 74 until 99 -> HRRanges.FAST
                in 100 until 1000 -> HRRanges.HIGH
                else -> {
                    HRRanges.NO_VALUE
                }
            }
        } else {
            return when (hrValue) {
                in 1 until 54 -> HRRanges.LOW
                in 55 until 65 -> HRRanges.EXCELLENT
                in 66 until 78 -> HRRanges.GOOD
                in 79 until 99 -> HRRanges.FAST
                in 100 until 1000 -> HRRanges.HIGH
                else -> {
                    HRRanges.NO_VALUE
                }
            }
        }
    }

}

enum class HRRanges {
    LOW, EXCELLENT, GOOD, FAST, HIGH, NO_VALUE
}