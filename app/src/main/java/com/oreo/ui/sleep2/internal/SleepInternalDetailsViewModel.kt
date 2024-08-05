package com.oreo.ui.sleep2.internal

import androidx.fragment.app.Fragment
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
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.OSleepInternalTrendsDataModel
import com.oreo.data.model.TrendAverage
import com.oreo.data.model.TrendsGraphData
import com.oreo.data.model.TrendsValues
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class SleepInternalDetailsViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

    lateinit var selectedLaunchMode: SleepInternalLaunchState
    val trendsData = HashMap<LocalDate, TrendsValues>()


    val reloadFragment = MutableLiveData<Event<SleepInternalLaunchState>>()

    var startDate: String = ""
    var endDate: String = ""

    private val _selectedPeriod = MutableLiveData<InternalSelectedPeriod>()
    val selectedPeriod: LiveData<InternalSelectedPeriod> = _selectedPeriod


    var dayAvg: TrendAverage? = null
    var weekAvg: TrendAverage? = null
    var monthAvg: TrendAverage? = null


    fun setSelectedPeriod(selectedPeriod: InternalSelectedPeriod) {
        fragments.value = null
        _selectedPeriod.value = selectedPeriod
    }

    private val _titleUpdate = MutableLiveData<Pair<String, Int>>()
    val titleUpdate: LiveData<Pair<String, Int>> = _titleUpdate

    init {
        val datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        endDate = LocalDate.now().format(datePattern)
        startDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
            .format(datePattern)
    }


    val fragments = MutableLiveData<List<Fragment>?>()
    var currentStartDate: LocalDate? = null

    fun getTrendsInternalDetailsData() {
        viewModelScope.launch(Dispatchers.IO) {
            if(isHealthMonitorTrend()){
                userActivityRepository.getSleepHealthMonitorTrendsPagesData(
                    startDate, endDate, selectedLaunchMode.key.lowercase()
                )
            }else{
                userActivityRepository.getSleepInternalTrendsPagesData(
                    startDate, endDate, selectedLaunchMode.key.lowercase()
                )
            }
            .collect { resource ->
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
                                        getTrendsInternalDetailsData()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            it.data?.forEach {
                                trendsData[LocalDate.parse(it.date)] = it
                            }

                            loadNewFragment()

                            if (dayAvg == null) {
                                dayAvg = it.dayAvg
                            }
                            if (weekAvg == null) {
                                weekAvg = it.weekAvg
                            }
                            if (monthAvg == null) {
                                monthAvg = it.monthAvg
                            }
                        }
                    }
                }
            }
        }
    }

    fun isHealthMonitorTrend():Boolean{
        val healthTrends = arrayListOf(
            SleepInternalLaunchState.RESPIRATORY_RATE,
            SleepInternalLaunchState.BLOOD_OXYGEN,
            SleepInternalLaunchState.HRV,
            SleepInternalLaunchState.RESTING_HEART_RATE,
            SleepInternalLaunchState.SKIN_TEMPERATURE)
        return selectedLaunchMode in healthTrends
    }

    /**
     * @return Pair of start date and end date
     */
    private fun getDatesToLoad(): Pair<LocalDate, LocalDate> {
        val (start, end) = when (selectedPeriod.value) {
            InternalSelectedPeriod.DAY, null -> {
                return if (currentStartDate == null) {
                    Pair(
                        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                        LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                    )
                } else {
                    Pair(
                        currentStartDate!!.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)),
                        currentStartDate!!.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY))
                    )
                }
            }

            InternalSelectedPeriod.WEEK -> {
                return if (currentStartDate == null) {
                    Pair(
                        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            .minusWeeks(5),
                        LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                    )
                } else {
                    Pair(
                        currentStartDate!!.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
                            .minusWeeks(5),
                        currentStartDate!!.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY))
                    )
                }
            }

            InternalSelectedPeriod.MONTH -> {
                if (currentStartDate == null) {
                    val start =
                        LocalDate.now().minusMonths(5).with(TemporalAdjusters.firstDayOfMonth())
                    val end = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())
                    Pair(start, end)
                } else {
                    val start =
                        currentStartDate!!.minusMonths(5).with(TemporalAdjusters.firstDayOfMonth())
                    val end = currentStartDate!!.with(TemporalAdjusters.lastDayOfMonth())
                    Pair(start, end)
                }
            }
        }

        return Pair(start, end)
    }

    private fun loadNewFragment() {

        val (start, end) = getDatesToLoad()


        if (end < LocalDate.parse(startDate)) {
            return
        }
        currentStartDate = start

        val dataToDisplay = ArrayList<TrendsValues>()

        var current = start
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        while (current <= end) {
            val data = trendsData[current]
            dataToDisplay.add(
                TrendsValues(
                    date = current.format(dateFormat), value1 = data?.value1, value2 = data?.value2
                )
            )
            current = current.plusDays(1)
        }

        val trendData = TrendsGraphData(
            data = dataToDisplay
        )

        getFragmentToAdd(trendData).let {
            var oldData = fragments.value
            if (oldData == null) {
                oldData = ArrayList()
            }
            (oldData as ArrayList).add(it)
            fragments.postValue(oldData)
        }
    }


    private fun getFragmentToAdd(trendData: TrendsGraphData): Fragment {
        trendData.contributorType = selectedLaunchMode
        when (selectedPeriod.value) {
            InternalSelectedPeriod.DAY, null -> {
                return when (selectedLaunchMode) {
                    SleepInternalLaunchState.REM_SLEEP, SleepInternalLaunchState.DEEP_SLEEP,
                    SleepInternalLaunchState.RESPIRATORY_RATE, SleepInternalLaunchState.BLOOD_OXYGEN,
                    SleepInternalLaunchState.LATENCY,
                    SleepInternalLaunchState.RESTFULNESS,
                    SleepInternalLaunchState.SLEEP_PERFORMANCE-> SleepBarChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.SLEEP_DURATION,
                    SleepInternalLaunchState.HRV,
                    SleepInternalLaunchState.RESTING_HEART_RATE,
                    SleepInternalLaunchState.SKIN_TEMPERATURE,
                    SleepInternalLaunchState.EFFICIENCY-> SleepSingleLineGradientChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.RESTORATIVE_SLEEP -> SleepMultiBarChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.HOUR_VS_NEED -> SleepMultiLineChartFragment.newInstance(
                        trendData
                    )


                    //pending from product
                    SleepInternalLaunchState.SLEEP_TIME -> SleepSingleLineChartFragment.newInstance(
                        trendData
                    )

                    else -> SleepBarChartFragment.newInstance(trendData)
                }
            }

            InternalSelectedPeriod.WEEK -> {
                return when (selectedLaunchMode) {
                    SleepInternalLaunchState.HOUR_VS_NEED, SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                        SleepMultiLineChart2Fragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.WEEK
                        })
                    }

                    SleepInternalLaunchState.SLEEP_TIME -> {
                        SleepSleepTImeChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.WEEK
                        })
                    }

                    SleepInternalLaunchState.RESPIRATORY_RATE, SleepInternalLaunchState.RESTING_HEART_RATE,
                    SleepInternalLaunchState.BLOOD_OXYGEN,
                    SleepInternalLaunchState.SKIN_TEMPERATURE,
                    SleepInternalLaunchState.HRV -> {
                        SleepSingleLineChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.WEEK
                        })
                    }

                    else -> {
                        SleepSingleLineChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.WEEK
                        })
                    }
                }
            }

            InternalSelectedPeriod.MONTH -> {
                return when (selectedLaunchMode) {
                    SleepInternalLaunchState.HOUR_VS_NEED, SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                        SleepMultiLineChart2Fragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.MONTH
                        })
                    }

                    SleepInternalLaunchState.SLEEP_TIME -> {
                        SleepSleepTImeChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.MONTH
                        })
                    }

                    SleepInternalLaunchState.RESPIRATORY_RATE, SleepInternalLaunchState.RESTING_HEART_RATE,
                    SleepInternalLaunchState.BLOOD_OXYGEN,
                    SleepInternalLaunchState.SKIN_TEMPERATURE,
                    SleepInternalLaunchState.HRV -> {
                        SleepSingleLineChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.MONTH
                        })
                    }

                    else -> {
                        SleepSingleLineChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.MONTH
                        })
                    }
                }
            }
        }


    }

    private fun getTitle(): Pair<String, Int> {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                Pair(
                    resourcesProvider.getString(R.string.text_restorative_sleep),
                    R.drawable.ic_sleep_restroactive_sp
                )
            }

            SleepInternalLaunchState.SLEEP_TIME -> Pair(
                resourcesProvider.getString(R.string.text_sleep_time), R.drawable.ic_sleep_time
            )

            SleepInternalLaunchState.HOUR_VS_NEED -> Pair(
                resourcesProvider.getString(R.string.text_hour_vs_need), R.drawable.ic_sleep_snooz
            )

            SleepInternalLaunchState.SLEEP_PERFORMANCE -> Pair(
                resourcesProvider.getString(R.string.text_sleep_performance),
                R.drawable.ic_sleep_performance
            )

            SleepInternalLaunchState.EFFICIENCY -> Pair(
                resourcesProvider.getString(R.string.text_efficiency),
                R.drawable.ic_sleep_efficiency
            )

            SleepInternalLaunchState.REM_SLEEP -> Pair(
                resourcesProvider.getString(R.string.text_rem_sleep), R.drawable.ic_sleep_rem_sp
            )

            SleepInternalLaunchState.DEEP_SLEEP -> Pair(
                resourcesProvider.getString(R.string.text_deep_sleep), R.drawable.ic_sleep_deep_sp
            )

            SleepInternalLaunchState.LATENCY -> Pair(
                resourcesProvider.getString(R.string.text_latency), R.drawable.ic_sleep_latency
            )

            SleepInternalLaunchState.RESTFULNESS -> Pair(
                resourcesProvider.getString(R.string.text_restfulness),
                R.drawable.ic_sleep_restfulness
            )

            SleepInternalLaunchState.SLEEP_DURATION -> Pair(
                resourcesProvider.getString(R.string.text_sleep_duration),
                R.drawable.ic_clock_off_sleep
            )

            SleepInternalLaunchState.TIMING -> Pair(
                resourcesProvider.getString(R.string.text_timing), R.drawable.ic_clock_off_sleep
            )
            SleepInternalLaunchState.RESPIRATORY_RATE -> {
                Pair(
                    resourcesProvider.getString(R.string.text_respiratory_rate),
                    R.drawable.ic_respiratory_rate
                )
            }

            SleepInternalLaunchState.RESTING_HEART_RATE -> Pair(
                resourcesProvider.getString(R.string.text_resting_heart_rate),
                R.drawable.ic_resting_hr
            )

            SleepInternalLaunchState.HRV -> Pair(
                resourcesProvider.getString(R.string.text_hrv),
                R.drawable.ic_hrv
            )

            SleepInternalLaunchState.SKIN_TEMPERATURE -> Pair(
                resourcesProvider.getString(R.string.text_skin_temperature),
                R.drawable.ic_skin_tempreature
            )

            SleepInternalLaunchState.BLOOD_OXYGEN -> Pair(
                resourcesProvider.getString(R.string.text_blood_oxygen),
                R.drawable.ic_blood_oxygen
            )
        }
    }

    fun updateTitle() {
        _titleUpdate.postValue(getTitle())
    }

    fun getLearnMoreData(): ArrayList<LearnMoreDataModel> {
        val dataList = ArrayList<LearnMoreDataModel>()
        dataList.add(
            LearnMoreDataModel(
                title = "General heart rate terms",
                msg = "2 min read",
                img = R.drawable.img_hr_article_1,
                type = 1
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "Normal heart rate for my age",
                msg = "2 min read",
                img = R.drawable.img_hr_article_2,
                type = 2
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "What are heart rate zones ?",
                msg = "2 min read",
                img = R.drawable.img_hr_article_3,
                type = 3
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "Heart rate during sleep",
                msg = "2 min read",
                img = R.drawable.img_hr_article_4,
                type = 4
            )
        )
        return dataList
    }

    fun loadMoreData() {
        loadNewFragment()
    }

    fun reloadData() {
        currentStartDate = null
        loadNewFragment()
    }

    fun getUnit(): String {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> ""
            SleepInternalLaunchState.SLEEP_PERFORMANCE -> "%"
            SleepInternalLaunchState.HOUR_VS_NEED -> ""
            SleepInternalLaunchState.SLEEP_TIME -> ""
            SleepInternalLaunchState.TIMING -> "min"
            SleepInternalLaunchState.EFFICIENCY -> "%"
            SleepInternalLaunchState.REM_SLEEP -> "min"
            SleepInternalLaunchState.DEEP_SLEEP -> "min"
            SleepInternalLaunchState.SLEEP_DURATION -> ""
            SleepInternalLaunchState.LATENCY -> "min"
            SleepInternalLaunchState.RESTFULNESS -> "times"
            SleepInternalLaunchState.RESPIRATORY_RATE -> ""
            SleepInternalLaunchState.RESTING_HEART_RATE -> "bpm"
            SleepInternalLaunchState.HRV -> ""
            SleepInternalLaunchState.SKIN_TEMPERATURE -> ""
            SleepInternalLaunchState.BLOOD_OXYGEN -> "%"
        }
    }

    fun getTopState(): TrendsTopState {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.SLEEP_DURATION, SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                TrendsTopState.SINGLE_DATE
            }

            SleepInternalLaunchState.HOUR_VS_NEED -> {
                TrendsTopState.DOUBLE_DATE
            }

            else -> {
                TrendsTopState.SINGLE
            }
        }
    }

    fun getTopDisplayDate(): String {
        val todayDate = LocalDate.now()
        return when (selectedPeriod.value) {
            InternalSelectedPeriod.DAY, null -> {
                val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
                todayDate.format(dayFormat)
            }

            InternalSelectedPeriod.WEEK -> {
                val weekFormatStart = DateTimeFormatter.ofPattern("dd MMMM")
                val weekFormatEnd = DateTimeFormatter.ofPattern("dd MMMM, yyyy")
                val weekStart = todayDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val weekEnd = todayDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

                "${weekStart.format(weekFormatStart)} - ${weekEnd.format(weekFormatEnd)}"
            }

            InternalSelectedPeriod.MONTH -> {
                val dayFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
                todayDate.format(dayFormat)
            }
        }

    }


}

enum class InternalSelectedPeriod {
    DAY, WEEK, MONTH
}

enum class TrendsTopState {
    SINGLE, SINGLE_DATE, DOUBLE_DATE
}