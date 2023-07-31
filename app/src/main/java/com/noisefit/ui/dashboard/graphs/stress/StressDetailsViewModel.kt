package com.noisefit.ui.dashboard.graphs.stress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.db.CacheResult
import com.noisefit_commans.data.model.StressType
import com.noisefit_commans.data.model.StressZoneAnalysis
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.model.history.StressHistory
import com.noisefit_commans.data.model.history.StressHistoryResponse
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.common.calculatePercentage
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.util.graph.StressBarChartUtils
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.StressDataBreakup
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class StressDetailsViewModel
@Inject
constructor(
    private val localDataStore: DataStoredInterface,
    private val userActivityRepository: UserActivityRepository,
    private val syncRepository: SyncRepository,
    var sessionManager: SessionManager
) : BaseViewModel() {

    private val _graphInterval = MutableLiveData<GraphInterval>()
    val graphInterval: LiveData<GraphInterval> = _graphInterval

    private val _stressZoneAnalysis = MutableLiveData<ArrayList<StressZoneAnalysis>>()
    val stressZoneAnalysis: LiveData<ArrayList<StressZoneAnalysis>> = _stressZoneAnalysis

    private var dailyHistoryResponse: StressHistoryResponse? = null
    private var weeklyHistoryResponse: StressHistoryResponse? = null
    private var yearlyHistoryResponse: StressHistoryResponse? = null
    private var monthlyHistoryResponse: StressHistoryResponse? = null
    private var todayStressHistoryData: StressHistory? = null

    private val _stressHistoryResponse = MutableLiveData<StressHistoryResponse>()
    val stressHistoryResponse: LiveData<StressHistoryResponse> = _stressHistoryResponse

    var selectedStartDate: String? = null
    var selectedEndDate: String? = null


    val mTodaysDate = DateFormats.getTodaysDateString(10)


    init {
        getStressHistory()
    }

    var selectedDate = DateFormats.getTodaysDateString(13)

    fun getYear(): String {
        return if (selectedEndDate.isNullOrEmpty()) {
            DateFormats.getTodaysDateString(12)
        } else {
            DateFormats.getFormattedYear(selectedEndDate)
        }
    }

    fun getCurrentMonth(stepsDataList: ArrayList<StressHistory>): String {
        return if (stepsDataList.isNullOrEmpty() || stepsDataList[0].date.isNullOrEmpty()) {
            DateFormats.getTodaysDateString(11)
        } else {

            DateFormats.formatDateTime(
                stepsDataList[0].date,
                DateFormats.dateFormat3,
                DateFormats.dateFormat4
            )

        }
    }

    fun getWeeklyMonth(stepsDataList: ArrayList<StressHistory>): String {
        return if (stepsDataList.isNullOrEmpty()) {
            ""
        } else {
            return "${DateFormats.formatWeeklyDateMonth(stepsDataList[0].date)} - ${
                DateFormats.formatWeeklyDateMonthWithYear(
                    stepsDataList.last().date
                )
            }"
        }
    }

    private fun handleStressResponse(stressDataList: List<StressDataBreakup>?): StressHistory {
        val date = DateFormats.getTodaysDateString(10)
        if (stressDataList.isNullOrEmpty()) {
            setStressStageList(0, 0, 0, 0)
            return StressHistory(
                date = date,
                month = 0,
                history_type = getGraphTypeText(),
                value = 0,
                maxCount = 0,
                minCount = 0,
                hourly_breakup = ArrayList()
            )
        }

        val minimum = stressDataList.minOf { it2 -> it2.value ?: 0 }
        val maximum = stressDataList.maxOf { it2 -> it2.value ?: 0 }
        val average = stressDataList.map { it2 -> it2.value ?: 0 }.average().roundToInt()


        val hourlyBreakupList = ArrayList<StressHistory>()

        var high = 0
        var medium = 0
        var normal = 0
        var relax = 0
        var total = 0

        stressDataList.forEach { stressDataBreakup ->

            total += 1
            when (StressBarChartUtils.getStressType(stressDataBreakup.value ?: 0)) {
                StressType.Relax -> {
                    relax += 1
                }
                StressType.Normal -> {
                    normal += 1
                }
                StressType.Medium -> {
                    medium += 1
                }
                StressType.High -> {
                    high += 1
                }
            }

            hourlyBreakupList.add(
                StressHistory(
                    date = date,
                    month = 0,
                    time = stressDataBreakup.time,
                    history_type = getGraphTypeText(),
                    value = stressDataBreakup.value,
                    maxCount = 0,
                    minCount = 0,
                    hourly_breakup = ArrayList()
                )
            )
        }

        val relaxPercentage = relax.toFloat().calculatePercentage(total.toFloat()).roundToInt()
        val normalPercentage = normal.toFloat().calculatePercentage(total.toFloat()).roundToInt()
        val mediumPercentage = medium.toFloat().calculatePercentage(total.toFloat()).roundToInt()
        val highPercentage = high.toFloat().calculatePercentage(total.toFloat()).roundToInt()

        setStressStageList(relaxPercentage, normalPercentage, mediumPercentage, highPercentage)


        return StressHistory(
            minCount = minimum,
            maxCount = maximum,
            month = 0,
            history_type = getGraphTypeText(),
            count = average,
            date = date,
            hourly_breakup = hourlyBreakupList
        )
    }

    fun setStressZoneAnalysis(stressDataList: List<StressHistory>?){
        var high = 0
        var medium = 0
        var normal = 0
        var relax = 0
        var total = 0

        stressDataList?.forEach { stressDataBreakup ->

            total += 1
            when (StressBarChartUtils.getStressType(stressDataBreakup.value ?: 0)) {
                StressType.Relax -> {
                    relax += 1
                }
                StressType.Normal -> {
                    normal += 1
                }
                StressType.Medium -> {
                    medium += 1
                }
                StressType.High -> {
                    high += 1
                }
            }

        }

        val relaxPercentage = relax.toFloat().calculatePercentage(total.toFloat()).roundToInt()
        val normalPercentage = normal.toFloat().calculatePercentage(total.toFloat()).roundToInt()
        val mediumPercentage = medium.toFloat().calculatePercentage(total.toFloat()).roundToInt()
        val highPercentage = high.toFloat().calculatePercentage(total.toFloat()).roundToInt()

        setStressStageList(relaxPercentage, normalPercentage, mediumPercentage, highPercentage)
    }
    private fun getStressHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getTodayStress().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        todayStressHistoryData = handleStressResponse(resource.value)
                        _graphInterval.postValue(GraphInterval.DAY)
                    }
                    is CacheResult.GenericError -> {
                        sendMessage(resource.errorMessage)
                    }
                }

            }

        }
    }

    fun getStressData() {
        viewModelScope.launch {

           /* when (_graphInterval.value) {
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

            userActivityRepository.getStressHistory(getGraphTypeText(),selectedStartDate,selectedEndDate).collect { resource ->
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
                                    getStressData()
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


    private fun setStressStageList(
        relaxPercentage: Int,
        normalPercentage: Int,
        mediumPercentage: Int,
        highPercentage: Int
    ) {
        val sleepStageList = ArrayList<StressZoneAnalysis>()
        sleepStageList.add(StressZoneAnalysis("Relaxed", "01-29", relaxPercentage, StressType.Relax))
        sleepStageList.add(
            StressZoneAnalysis(
                "Normal",
                "30-59",
                normalPercentage,
                StressType.Normal
            )
        )
        sleepStageList.add(
            StressZoneAnalysis(
                "Medium",
                "60-79",
                mediumPercentage,
                StressType.Medium
            )
        )
        sleepStageList.add(StressZoneAnalysis("High", "80-99", highPercentage, StressType.High))
        _stressZoneAnalysis.postValue(sleepStageList)
    }

    private fun setTodayDataInList(
        onlineStepsHistoryData: StressHistoryResponse
    ) {
        when (_graphInterval.value) {
            GraphInterval.DAY -> {
                dailyHistoryResponse = onlineStepsHistoryData

                val updateTodaysData = if (selectedEndDate.isNullOrEmpty()) {
                    true
                } else {
                    mTodaysDate.equals(selectedEndDate, true)
                }

                if (updateTodaysData) {
                    onlineStepsHistoryData.history?.removeLast()
                    todayStressHistoryData?.let { onlineStepsHistoryData.history?.add(it) }
                }

            }
            GraphInterval.WEEK -> {
                weeklyHistoryResponse = onlineStepsHistoryData
            }
            GraphInterval.MONTH -> {
                monthlyHistoryResponse = onlineStepsHistoryData
            }
            GraphInterval.YEAR -> {
                yearlyHistoryResponse = onlineStepsHistoryData
            }
            else -> {
                throw NullPointerException("invalid graph Interval value")
            }
        }

        _stressHistoryResponse.postValue(onlineStepsHistoryData)
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
}