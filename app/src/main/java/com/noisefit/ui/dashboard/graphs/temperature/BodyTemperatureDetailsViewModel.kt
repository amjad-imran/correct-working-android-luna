package com.noisefit.ui.dashboard.graphs.temperature

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.model.history.BodyTempHistory
import com.noisefit_commans.data.model.history.BodyTempHistoryResponse
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.BodyTemperatureBreakup
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt


@HiltViewModel
class BodyTemperatureDetailsViewModel
@Inject
constructor(
    localDataStore: DataStoredInterface,
    private val userActivityRepository: UserActivityRepository,
    private val syncRepository: SyncRepository,
    private val dataUnitConverter: DataUnitConverter,
    val sessionManager: SessionManager
) : BaseViewModel() {

    private val _graphInterval = MutableLiveData<GraphInterval>()
    val graphInterval: LiveData<GraphInterval> = _graphInterval

    private var tempUnit: Units? = null

    private var dailyHistoryResponse: BodyTempHistoryResponse? = null
    private var weeklyHistoryResponse: BodyTempHistoryResponse? = null
    private var yearlyHistoryResponse: BodyTempHistoryResponse? = null
    private var monthlyHistoryResponse: BodyTempHistoryResponse? = null
    private var todayBodyTempData: BodyTempHistory? = null

    private val _bodyTempHistoryResponse = MutableLiveData<BodyTempHistoryResponse>()
    val bodyTempHistoryResponse: LiveData<BodyTempHistoryResponse> = _bodyTempHistoryResponse

    var selectedStartDate: String? = null
    var selectedEndDate: String? = null

    val mTodaysDate = DateFormats.getTodaysDateString(10)


    init {
        tempUnit = localDataStore.getBodyTempUnit()
        getBodyTempHistory()
    }


    var selectedDate = DateFormats.getTodaysDateString(13)

    fun getYear(): String {
        return if (selectedEndDate.isNullOrEmpty()) {
            DateFormats.getTodaysDateString(12)
        } else {
            DateFormats.getFormattedYear(selectedEndDate)
        }
    }

    fun getCurrentMonth(bodyTempDataList: ArrayList<BodyTempHistory>): String {
        return if (bodyTempDataList.isEmpty() || bodyTempDataList[0].date.isNullOrEmpty()) {
            DateFormats.getTodaysDateString(11)
        } else {

            DateFormats.formatDateTime(
                bodyTempDataList[0].date,
                DateFormats.dateFormat3,
                DateFormats.dateFormat4
            )

        }
    }

    fun getWeeklyMonth(bodyTempDataList: ArrayList<BodyTempHistory>): String {
        return if (bodyTempDataList.isEmpty()) {
            ""
        } else {
            return "${DateFormats.formatWeeklyDateMonth(bodyTempDataList[0].date)} - ${
                DateFormats.formatWeeklyDateMonthWithYear(
                    bodyTempDataList.last().date
                )
            }"
        }
    }

    private fun handleBodyTempResponse(bloodTempDataList: List<BodyTemperatureBreakup>?): BodyTempHistory {
        val date = DateFormats.getTodaysDateString(10)
        if (bloodTempDataList.isNullOrEmpty()) {
            return BodyTempHistory(
                date = date,
                month = 0,
                history_type = getGraphTypeText(),
                value = 0f,
                maxCount = 0f,
                minCount = 0f,
                hourly_breakup = ArrayList()
            )
        }

        val minimum = bloodTempDataList.minOf { it2 -> it2.value ?: 0f }
        val maximum = bloodTempDataList.maxOf { it2 -> it2.value ?: 0f }
        val average = bloodTempDataList.map { it2 -> it2.value ?: 0f }.average().roundToInt()


        val hourlyBreakupList = ArrayList<BodyTempHistory>()

        bloodTempDataList.forEach { bodyTempDataBreakup ->

            hourlyBreakupList.add(
                BodyTempHistory(
                    date = date,
                    month = 0,
                    time = bodyTempDataBreakup.time,
                    history_type = getGraphTypeText(),
                    value = bodyTempDataBreakup.value,
                    maxCount = 0f,
                    minCount = 0f,
                    hourly_breakup = ArrayList()
                )
            )
        }

        return BodyTempHistory(
            minCount = minimum,
            maxCount = maximum,
            month = 0,
            history_type = getGraphTypeText(),
            count = average.toFloat(),
            date = date,
            hourly_breakup = hourlyBreakupList
        )
    }

    private fun getBodyTempHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getTodayBodyTemp().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        todayBodyTempData = handleBodyTempResponse(resource.value)
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

            userActivityRepository.getBodyTempHistory(getGraphTypeText(),selectedStartDate,selectedEndDate).collect { resource ->
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


    private fun setTodayDataInList(
        onlineStepsHistoryData: BodyTempHistoryResponse
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
                    todayBodyTempData?.let { onlineStepsHistoryData.history?.add(it) }
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

        _bodyTempHistoryResponse.postValue(onlineStepsHistoryData)
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

    fun getBodyTempWithUnit(value: Float): String {
        return "${
            dataUnitConverter.formatBodyTemp(
                value,
                tempUnit!!
            )
        } ${dataUnitConverter.bodyTempUnit(tempUnit)}"
    }

    fun getBodyTemp(value: Float): Float {
        return dataUnitConverter.formatBodyTempInFloat(
            value,
            tempUnit!!
        )
    }
}