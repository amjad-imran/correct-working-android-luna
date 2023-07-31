package com.noisefit.ui.dashboard.graphs.bloodOxygen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.model.history.BoHistory
import com.noisefit_commans.data.model.history.BoHistoryResponse
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.common.calculatePercentage
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.BloodOxygenBreakup
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt


@HiltViewModel
class BloodOxygenDetailsViewModel
@Inject
constructor(
    private val localDataStore: DataStoredInterface,
    private val userActivityRepository: UserActivityRepository,
    private val syncRepository: SyncRepository,
    val sessionManager: SessionManager
) : BaseViewModel() {

    private val _graphInterval = MutableLiveData<GraphInterval>()
    val graphInterval: LiveData<GraphInterval> = _graphInterval

    private val _spo2ProgressPercent = MutableLiveData<Int>()
    val spo2ProgressPercent: LiveData<Int> = _spo2ProgressPercent

    private var dailyHistoryResponse: BoHistoryResponse? = null
    private var weeklyHistoryResponse: BoHistoryResponse? = null
    private var yearlyHistoryResponse: BoHistoryResponse? = null
    private var monthlyHistoryResponse: BoHistoryResponse? = null
    private var todayBOHistoryData: BoHistory? = null

    private val _boHistoryResponse = MutableLiveData<BoHistoryResponse>()
    val boHistoryResponse: LiveData<BoHistoryResponse> = _boHistoryResponse

    var selectedStartDate: String? = null
    var selectedEndDate: String? = null

    val mTodaysDate = DateFormats.getTodaysDateString(10)


    init {
        getBoHistory()
    }

    var selectedDate = DateFormats.getTodaysDateString(13)

    fun getYear(): String {
        return if (selectedEndDate.isNullOrEmpty()) {
            DateFormats.getTodaysDateString(12)
        } else {
            DateFormats.getFormattedYear(selectedEndDate)
        }
    }


    fun updateSpo2GoalPercentage(spo2Level: Int?) {
        val progressPercent =
            spo2Level?.toFloat()
                ?.calculatePercentage(100f)
        if (progressPercent != null) {
            _spo2ProgressPercent.postValue(progressPercent.roundToInt())
        } else {
            _spo2ProgressPercent.postValue(0)
        }
    }

    fun getCurrentMonth(stepsDataList: ArrayList<BoHistory>): String {
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

    fun getWeeklyMonth(stepsDataList: ArrayList<BoHistory>): String {
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

    private fun handleBOResponse(boDataList: List<BloodOxygenBreakup>?): BoHistory {
        val date = DateFormats.getTodaysDateString(10)
        if (boDataList.isNullOrEmpty()) {
            return BoHistory(
                date = date,
                month = 0,
                history_type = getGraphTypeText(),
                value = 0,
                maxCount = 0,
                minCount = 0,
                hourly_breakup = ArrayList()
            )
        }

        val minimum = boDataList.minOf { it2 -> it2.value ?: 0 }
        val maximum = boDataList.maxOf { it2 -> it2.value ?: 0 }
        val average = boDataList.map { it2 -> it2.value ?: 0 }.average().roundToInt()


        val hourlyBreakupList = ArrayList<BoHistory>()

        boDataList.forEach { boDataBreakup ->


            hourlyBreakupList.add(
                BoHistory(
                    date = date,
                    month = 0,
                    time = boDataBreakup.time,
                    history_type = getGraphTypeText(),
                    value = boDataBreakup.value,
                    maxCount = 0,
                    minCount = 0,
                    hourly_breakup = ArrayList()
                )
            )
        }


        return BoHistory(
            minCount = minimum,
            maxCount = maximum,
            month = 0,
            history_type = getGraphTypeText(),
            count = average,
            date = date,
            hourly_breakup = hourlyBreakupList
        )
    }

    private fun getBoHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getTodayBloodOxygen().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        todayBOHistoryData = handleBOResponse(resource.value)
                        updateSpo2GoalPercentage(todayBOHistoryData?.count)
                        _graphInterval.postValue(GraphInterval.DAY)
                    }
                    is CacheResult.GenericError -> {
                        sendMessage(resource.errorMessage)
                    }
                }

            }

        }
    }

    fun getBoData() {
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

            userActivityRepository.getBloodOxygenHistory(
                getGraphTypeText(), selectedStartDate,
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
                                    getBoData()
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
        onlineStepsHistoryData: BoHistoryResponse
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
                    todayBOHistoryData?.let { onlineStepsHistoryData.history?.add(it) }
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

        _boHistoryResponse.postValue(onlineStepsHistoryData)
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