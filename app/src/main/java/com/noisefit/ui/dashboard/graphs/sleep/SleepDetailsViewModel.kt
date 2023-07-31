package com.noisefit.ui.dashboard.graphs.sleep

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.dataConverter.OnlineDataMapper
import com.noisefit.data.local.db.CacheResult
import com.noisefit_commans.data.model.*
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BedTimeVariance
import com.noisefit_commans.data.response.BedTimeVarianceBreakup
import com.noisefit_commans.data.response.SleepBlogCategories
import com.noisefit_commans.data.response.SleepHighlightResponse
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.enums.SleepExtraType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.response.SleepBreakup
import com.noisefit_commans.response.SleepHistoryResponse
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SleepDetailsViewModel @Inject constructor(
    private val localDataStore: DataStoredInterface,
    private val userActivityRepository: UserActivityRepository,
    private val syncRepository: SyncRepository,
    private val onlineDataMapper: OnlineDataMapper,
    val sessionManager: SessionManager,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {

    private val _graphInterval = MutableLiveData<GraphInterval>()
    val graphInterval: LiveData<GraphInterval> = _graphInterval

    private var dailyHistoryResponse: SleepHistoryResponse? = null
    private var weeklyHistoryResponse: SleepHistoryResponse? = null
    private var yearlyHistoryResponse: SleepHistoryResponse? = null
    private var monthlyHistoryResponse: SleepHistoryResponse? = null
    private var todaySleepHistoryData: SleepBreakup? = null

    private val _sleepHistoryResponse = MutableLiveData<SleepHistoryResponse>()
    val sleepHistoryResponse: LiveData<SleepHistoryResponse> = _sleepHistoryResponse

    private val _sleepHighlightResponse = MutableLiveData<SleepHighlightResponse>()
    val sleepHighlightResponse: LiveData<SleepHighlightResponse> = _sleepHighlightResponse

    private val _sleepBlogs = MutableLiveData<List<SleepBlogCategories>>()
    val sleepBlogs: LiveData<List<SleepBlogCategories>> = _sleepBlogs

    var selectedStartDate: String? = null
    var selectedEndDate: String? = null

    val mTodaysDate = DateFormats.getTodaysDateString(10)


    init {
        getSleepHistory()
        getSleepBlogs()
    }


    private fun getSleepHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getTodaySleep().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        todaySleepHistoryData = (resource.value)

                        _graphInterval.postValue(GraphInterval.DAY)
                    }
                    is CacheResult.GenericError -> {
                        sendMessage(resource.errorMessage)
                    }
                }

            }

        }
    }

    private fun setTodayDataInList(
        onlineStepsHistoryData: SleepHistoryResponse
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
                    onlineStepsHistoryData.sleepHistory?.removeLast()
                    todaySleepHistoryData?.let { onlineStepsHistoryData.sleepHistory?.add(it) }
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
        _sleepHistoryResponse.postValue(onlineStepsHistoryData)
    }

    fun getSleepData() {
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
            userActivityRepository.getSleepHistory(
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
                                    getSleepData()
                                    if (_sleepHighlightResponse.value == null) {
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

                        }
                    }
                }
            }
        }
    }

    fun getHighlights() {
        viewModelScope.launch {
            userActivityRepository.getSleepHighlights().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        /* setApiErrors(resource.response.apply {
                             this.uiComponentType as UIComponentType.RetryApiDialog
                             this.uiComponentType.callback = object : BinaryActionCallback {
                                 override fun yes() {
                                     getHighlights()
                                 }

                                 override fun no() {

                                 }
                             }
                         })*/
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _sleepHighlightResponse.postValue(it)
                        }
                    }
                }
            }
        }
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

    private fun getSleepBlogs() {
        viewModelScope.launch {
            userActivityRepository.getSleepBlogs().collect { resource ->
                when (resource) {
                    is Resource.NetworkError -> {

                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _sleepBlogs.postValue(it)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun getWeeklyMonth(data: List<SleepBreakup>): String {
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

    fun getYear(): String {
        return if (selectedEndDate.isNullOrEmpty()) {
            DateFormats.getTodaysDateString(12)
        } else {
            DateFormats.getFormattedYear(selectedEndDate)
        }
    }

    fun getCurrentMonth(data: List<SleepBreakup>): String {
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

    fun parseBedTimeVariance(variance: BedTimeVariance): LineData {
        val values = java.util.ArrayList<Entry>()

        variance.breakup?.forEachIndexed { index, bedTimeVarianceBreakup ->
            values.add(
                Entry(
                    index.toFloat(),
                    bedTimeVarianceBreakup.duration_diff?.toFloat() ?: 0.0f
                )
            )
        }

        // create a dataset and give it a type
        val set1 = LineDataSet(values, "BedTime Variance")
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);
        set1.lineWidth = 1.75f
        set1.circleRadius = 5f
        set1.circleHoleRadius = 2.5f
        set1.color = Color.WHITE//Color.parseColor("#262B2F")//
        set1.setCircleColor(Color.WHITE)
        set1.highLightColor = Color.WHITE
        set1.setDrawValues(false)

        // create a data object with the data sets
        return LineData(set1)
    }

    fun getXAxisMarker(variance: BedTimeVariance): ArrayList<String> {
        if (variance.breakup == null) {
            return ArrayList()
        } else {
            return getWeekly(variance.breakup!!)
        }
    }

    private fun getWeekly(breakup: List<BedTimeVarianceBreakup>): ArrayList<String> {
        val labelList = ArrayList<String>()
        breakup.forEachIndexed { index, bedTimeVarianceBreakup ->
            if (bedTimeVarianceBreakup.date != null) {
                labelList.add(DateFormats.formatWeek(bedTimeVarianceBreakup.date))
            }
        }
        return labelList
    }

    fun getHrMaxMinValues(): String {
        return if (sleepHistoryResponse.value == null) {
            "_-_bpm"
        } else {
            "${sleepHistoryResponse.value!!.hr_min}-${sleepHistoryResponse.value!!.hr_max}bpm"
        }
    }

    fun getStressMaxMinValues(): String {
        return if (sleepHistoryResponse.value == null) {
            "_-_"
        } else {
            "${sleepHistoryResponse.value!!.stress_min}-${sleepHistoryResponse.value!!.stress_max}"
        }
    }

    fun generateSleepExtraData(
        breakup: SleepBreakup,
        value: SleepExtraType?
    ): java.util.ArrayList<SleepExtraData> {
        val data = ArrayList<SleepExtraData>()

        val isHeartSelected = if (value != null) {
            value == SleepExtraType.HeartRate
        } else false

        val isStressSelected = if (value != null) {
            value == SleepExtraType.StressLevel
        } else false

        data.add(
            SleepExtraData(
                "Heart Rate",
                "${breakup.hr_min ?: "_"}-${breakup.hr_max ?: "_"}bpm",
                SleepExtraType.HeartRate,
                isHeartSelected
            )
        )

        if (!breakup.stress_breakup.isNullOrEmpty()) {
            data.add(
                SleepExtraData(
                    "Stress Levels",
                    "${breakup.stress_min ?: "_"}-${breakup.stress_max ?: "_"}",
                    SleepExtraType.StressLevel,
                    isStressSelected
                )
            )
        }
        return data
    }



}