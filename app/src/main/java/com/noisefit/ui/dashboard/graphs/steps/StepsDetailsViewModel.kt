package com.noisefit.ui.dashboard.graphs.steps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.GraphHighlightResponse
import com.noisefit_commans.data.model.history.StepsHistoryData
import com.noisefit_commans.data.model.history.StepsHistoryResponse
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.common.calculatePercentage
import com.noisefit_commans.data.enums.HealthOverViewHistoryType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.StepsData
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

@HiltViewModel
class StepsDetailsViewModel
@Inject
constructor(
    localDataStore: DataStoredInterface,
    private val userActivityRepository: UserActivityRepository,
    private val syncRepository: SyncRepository,
    private val dataUnitConverter: DataUnitConverter,
    val sessionManager: SessionManager
) : BaseViewModel() {


    var unit = Units.METRIC
    var distanceUnit = ""


    var stepsGoal: Int = 0
    var caloriesGoal: Int = 0
    var distanceGoal: String = ""
    var distanceGoalInMeter: Int = 0
    private var dailyHistoryResponse: StepsHistoryResponse? = null
    private var weeklyHistoryResponse: StepsHistoryResponse? = null
    private var yearlyHistoryResponse: StepsHistoryResponse? = null
    private var monthlyHistoryResponse: StepsHistoryResponse? = null
    private var todayStepsHistoryData: StepsHistoryData? = null
    private val _graphInterval = MutableLiveData(GraphInterval.DAY)
    val graphInterval: LiveData<GraphInterval> = _graphInterval

    private val _highlightsLoading = MutableLiveData<Boolean>()

    val highlightLoading: LiveData<Boolean> = _highlightsLoading

    private val _stepsHistoryResponse = MutableLiveData<StepsHistoryResponse>()
    val stepsHistoryResponse: LiveData<StepsHistoryResponse> = _stepsHistoryResponse

    private val _stepsOfflineHistory = MutableLiveData<StepsHistoryResponse>()
    val stepsOfflineHistory: LiveData<StepsHistoryResponse> = _stepsOfflineHistory

    private val _highlightResponse = MutableLiveData<GraphHighlightResponse>()
    val highlightResponse: LiveData<GraphHighlightResponse> = _highlightResponse

    private val _stepsProgressPercent = MutableLiveData<Int>()
    val stepsProgressPercent: LiveData<Int> = _stepsProgressPercent


    var selectedDate = DateFormats.getTodaysDateString(13)

    var selectedStartDate: String? = null
    var selectedEndDate: String? = null

    val mTodaysDate = DateFormats.getTodaysDateString(10)


    /**
     * isStep true ->Show Step UI
     * isStep false ->Show Distance UI
     */
    var healthOverViewHistoryType: HealthOverViewHistoryType = HealthOverViewHistoryType.Steps


    init {
        val user = localDataStore.getUser()
        unit = user?.userGoals?.getUnit() ?: Units.METRIC
        stepsGoal = user?.userGoals?.stepGoal ?: 0
        caloriesGoal = user?.userGoals?.caloriesGoal ?: 0
        distanceGoalInMeter = user?.userGoals?.distanceGoal ?: 0
        distanceGoal = getDistanceGoal(distanceGoalInMeter)
        distanceUnit = dataUnitConverter.distanceUnit(unit)
    }

    fun getYear(): String {
        return if (selectedEndDate.isNullOrEmpty()) {
            DateFormats.getTodaysDateString(12)
        } else {
            DateFormats.getFormattedYear(selectedEndDate)
        }
    }

    fun getCurrentMonth(stepsDataList: ArrayList<StepsHistoryData>): String {
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

    fun getWeeklyMonth(stepsDataList: ArrayList<StepsHistoryData>): String {
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


    private fun handleOfflineStepsData(dbStepsData: StepsData?): StepsHistoryData {
        val hourlyBreakup = ArrayList<StepsHistoryData>()

        val todayDate = DateFormats.getTodaysDateString(10)
        val overallHistory = StepsHistoryData()
        overallHistory.apply {
            distance = dbStepsData?.totalDistance?.toLong()
            active_time = dbStepsData?.totalActiveTime
            calories = dbStepsData?.totalCalories?.toLong()
            date = todayDate
            hour_of_the_day = dbStepsData?.hourOfTheDay
            steps = dbStepsData?.totalSteps?.toLong()
        }

        dbStepsData?.stepArray?.forEach { stepArray ->
            val stepData = StepsHistoryData()
            stepData.distance = stepArray.distance.toLong()
            stepData.active_time = stepArray.activeTime
            stepData.calories = stepArray.calories.toLong()
            stepData.date = todayDate
            stepData.hour_of_the_day = stepArray.hourOfTheDay
            stepData.steps = stepArray.steps.toLong()

            hourlyBreakup.add(stepData)
        }
        overallHistory.hourly_breakup = hourlyBreakup.toList()
        return overallHistory
    }

    fun getDataUnitConverter(): DataUnitConverter {
        return dataUnitConverter
    }

    fun updateStepsGoalPercentage(totalSteps: Long?) {
        val progressPercent =
            totalSteps?.toFloat()
                ?.calculatePercentage(stepsGoal.toFloat())
        if (progressPercent != null) {
            _stepsProgressPercent.postValue(progressPercent.roundToInt())
        } else {
            _stepsProgressPercent.postValue(0)
        }
    }

    fun updateDistanceGoalPercentage(distance: Long?) {
        val progressPercent =
            distance?.toFloat()
                ?.calculatePercentage(distanceGoalInMeter.toFloat())
        if (progressPercent != null) {
            _stepsProgressPercent.postValue(progressPercent.roundToInt())
        } else {
            _stepsProgressPercent.postValue(0)
        }
    }

    fun updateGoalCaloriesPercentage(calories: Long?) {
        val progressPercent =
            calories?.toFloat()
                ?.calculatePercentage(caloriesGoal.toFloat())
        if (progressPercent != null) {
            _stepsProgressPercent.postValue(progressPercent.roundToInt())
        } else {
            _stepsProgressPercent.postValue(0)
        }
    }

    fun getStepsPercentage() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getTodaySteps().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        todayStepsHistoryData = handleOfflineStepsData(resource.value)
                        when (healthOverViewHistoryType) {
                            HealthOverViewHistoryType.Steps -> {
                                updateStepsGoalPercentage(resource.value?.totalSteps?.toLong())
                            }
                            HealthOverViewHistoryType.Distance -> {
                                updateDistanceGoalPercentage(resource.value?.totalDistance?.toLong())
                            }
                            else -> {
                                updateGoalCaloriesPercentage(resource.value?.totalCalories?.toLong())
                            }
                        }

                    }
                    is CacheResult.GenericError -> {
                        sendMessage(resource.errorMessage)
                    }
                }

            }

        }
    }


    private fun getDistanceGoal(distance: Int): String {
        return dataUnitConverter.formatDistanceGoal(
            distance, unit
        )
    }

    fun getStepsData() {
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

            userActivityRepository.getStepsHistory(
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
                                    getStepsData()
                                    if (_highlightResponse.value == null) {
                                        getHighlights()
                                    }
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
        onlineStepsHistoryData: StepsHistoryResponse
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
                    todayStepsHistoryData?.let {
                        onlineStepsHistoryData.step_activities?.removeLast()
                        onlineStepsHistoryData.step_activities?.add(it)

                    }
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

        _stepsHistoryResponse.postValue(onlineStepsHistoryData)
    }

    fun getHighlights() {
        viewModelScope.launch {
            userActivityRepository.getStepsHighlights().collect { resource ->
                when (resource) {
                    is Resource.NetworkError -> {

                    }
                    is Resource.Loading -> {
                        _highlightsLoading.value = resource.loading
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _highlightResponse.postValue(it)
                        }
                    }
                    else -> {}
                }
            }
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

    private fun getGraphTypeText(): String {
        return when (_graphInterval.value) {
            GraphInterval.DAY -> "daily"
            GraphInterval.WEEK -> "weekly"
            GraphInterval.MONTH -> "monthly"
            GraphInterval.YEAR -> "yearly"
            else -> ""
        }
    }

    fun calculateProgress(data1: Long?, data2: Long?): Pair<Int, Int> {
        if (data1 == null && data2 == null) {
            return Pair(0, 0)
        }

        if (data1 == 0L) {
            return if (data2 == 0L) {
                Pair(0, 0)
            } else {
                Pair(0, 100)
            }
        }
        if (data2 == 0L) {
            return if (data1 == 0L) {
                Pair(0, 0)
            } else {
                Pair(100, 0)
            }
        }
        return try {
            val maxValue = if (data1 ?: 0 > data2 ?: 0) {
                data1
            } else {
                data2
            }?.toDouble()
            val firstPercent: Double = (data1!! / maxValue!!) * 100
            val secondPercent: Double = (data2!! / maxValue) * 100
            Pair(firstPercent.roundToInt(), secondPercent.roundToInt())
        } catch (exp: Exception) {
            Pair(0, 0)
        }
    }

    fun calculateProgress(data1: Int?, data2: Int?): Pair<Int, Int> {
        if (data1 == null && data2 == null) {
            return Pair(0, 0)
        }

        if (data1 == 0) {
            return if (data2 == 0) {
                Pair(0, 0)
            } else {
                Pair(0, 100)
            }
        }
        if (data2 == 0) {
            return if (data1 == 0) {
                Pair(0, 0)
            } else {
                Pair(100, 0)
            }
        }
        return try {
            val maxValue = if (data1 ?: 0 > data2 ?: 0) {
                data1
            } else {
                data2
            }?.toDouble()
            val firstPercent: Double = (data1!! / maxValue!!) * 100
            val secondPercent: Double = (data2!! / maxValue) * 100
            Pair(firstPercent.roundToInt(), secondPercent.roundToInt())
        } catch (exp: Exception) {
            Pair(0, 0)
        }
    }

}

enum class GraphInterval {
    DAY, WEEK, MONTH, YEAR
}