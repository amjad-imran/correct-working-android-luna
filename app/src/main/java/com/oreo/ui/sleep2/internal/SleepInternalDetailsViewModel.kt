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

    private val _selectedPeriod =
        MutableLiveData<InternalSelectedPeriod>()
    val selectedPeriod: LiveData<InternalSelectedPeriod> = _selectedPeriod


    var dayAvg: TrendAverage? = null
    var weekAvg: TrendAverage? = null
    var monthAvg: TrendAverage? = null


    fun setSelectedPeriod(selectedPeriod: InternalSelectedPeriod) {
        fragments.value = null
        _selectedPeriod.value = selectedPeriod
    }

    private val _titleUpdate =
        MutableLiveData<Pair<String, Int>>()
    val titleUpdate: LiveData<Pair<String, Int>> = _titleUpdate

    init {
        val datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        endDate = LocalDate.now().format(datePattern)
        startDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
            .format(datePattern)
    }


    var dataTill: LocalDate? = null
    val fragments = MutableLiveData<List<Fragment>?>()
    var currentStartDate: LocalDate? = null

    fun getTrendsInternalDetailsData() {
        viewModelScope.launch(Dispatchers.IO) {
            userActivityRepository.getSleepInternalTrendsPagesData(
                startDate, endDate, selectedLaunchMode.key.lowercase()
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

                            it.data?.firstOrNull()?.date?.let {
                                dataTill = LocalDate.parse(it)
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
                val start = LocalDate.now().minusMonths(5).with(TemporalAdjusters.firstDayOfMonth())
                val end = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())
                Pair(start, end)
            }
        }

        return Pair(start, end)
    }

    private fun loadNewFragment() {

        val (start, end) = getDatesToLoad()

        currentStartDate = start

        val dataToDisplay = ArrayList<TrendsValues>()

        var current = start
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        while (current <= end) {
            val data = trendsData[current]
            dataToDisplay.add(
                TrendsValues(
                    date = current.format(dateFormat),
                    value1 = data?.value1,
                    value2 = data?.value2
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
                    SleepInternalLaunchState.REM_SLEEP,
                    SleepInternalLaunchState.DEEP_SLEEP -> SleepBarChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.SLEEP_DURATION -> SleepSingleLineGradientChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.EFFICIENCY -> SleepSingleLineGradientChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.SLEEP_PERFORMANCE -> SleepBarChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.LATENCY -> SleepBarChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.RESTFULNESS -> SleepBarChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.RESTORATIVE_SLEEP -> SleepMultiBarChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.HOUR_VS_NEED -> SleepMultiLineChartFragment.newInstance(
                        trendData
                    )


                    //pending from product
                    SleepInternalLaunchState.SLEEP_TIME ->
                        SleepSingleLineChartFragment.newInstance(trendData)

                    else -> SleepBarChartFragment.newInstance(trendData)
                }
            }

            InternalSelectedPeriod.WEEK -> {
                return when (selectedLaunchMode) {
                    SleepInternalLaunchState.HOUR_VS_NEED -> {
                        SleepMultiLineChart2Fragment.newInstance(trendData.apply {
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
                return SleepSingleLineChartFragment.newInstance(trendData.apply {
                    this.selectedPeriod = InternalSelectedPeriod.MONTH
                })
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
                resourcesProvider.getString(R.string.text_sleep_time),
                R.drawable.ic_sleep_time
            )

            SleepInternalLaunchState.HOUR_VS_NEED -> Pair(
                resourcesProvider.getString(R.string.text_hour_vs_need),
                R.drawable.ic_sleep_snooz
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
                resourcesProvider.getString(R.string.text_rem_sleep),
                R.drawable.ic_sleep_rem_sp
            )

            SleepInternalLaunchState.DEEP_SLEEP -> Pair(
                resourcesProvider.getString(R.string.text_deep_sleep),
                R.drawable.ic_sleep_deep_sp
            )

            SleepInternalLaunchState.LATENCY -> Pair(
                resourcesProvider.getString(R.string.text_latency),
                R.drawable.ic_sleep_latency
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
                resourcesProvider.getString(R.string.text_timing),
                R.drawable.ic_clock_off_sleep
            )

            else -> {
                Pair(
                    resourcesProvider.getString(R.string.text_timing),
                    R.drawable.ic_clock_off_sleep
                )
            }
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

    fun getHighlightBackType(type: Int): Pair<Int, Int> {
        /*
        * 0-up
        * 1-warning
        * 2-red alert
        * */
        val background: Int
        val textColor: Int
        when (type) {
            0 -> {
                background = R.drawable.back_hm_optimal
                textColor = R.color.edit_text_color
            }

            1 -> {
                background = R.drawable.back_hm_fair
                textColor = R.color.color_trends_warning
            }

            else -> {
                background = R.drawable.back_hm_warning
                textColor = R.color.oreo_contributor_warning
            }
        }
        return Pair(background, textColor)
    }

    fun returnTrendsArrow(type: SleepInternalLaunchState): Int {
        val icon: Int = when (type) {
            SleepInternalLaunchState.SLEEP_PERFORMANCE, SleepInternalLaunchState.HOUR_VS_NEED, SleepInternalLaunchState.RESTORATIVE_SLEEP -> R.drawable.ic_trend_up
            SleepInternalLaunchState.EFFICIENCY, SleepInternalLaunchState.RESTFULNESS, SleepInternalLaunchState.LATENCY, SleepInternalLaunchState.SLEEP_DURATION -> R.drawable.ic_hm_tick
            else -> 0
        }
        return icon

    }

    private fun findLastSixMonthDatesList(): List<TrendsValues> {
        val currentDate = LocalDate.now()
        val sixMonthsAgo = currentDate.minusMonths(6)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val datesList = mutableListOf<String>()
        var date = sixMonthsAgo

        while (!date.isAfter(currentDate)) {
            datesList.add(date.format(formatter))
            date = date.plusDays(1)
        }

        val graphData = mutableListOf<TrendsValues>()
        println("List of Dates from 6 Months Ago to Current Date: ${datesList.size}")
        var i = 0
        for (formattedDate in datesList) {
            i++
            val ch = TrendsValues()
            ch.date = formattedDate
            ch.value1 = 3600 + i
            ch.value2 = 200
        }

        return graphData.takeLast(7)
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
            SleepInternalLaunchState.RESTING_HEART_RATE -> ""
            SleepInternalLaunchState.HRV -> ""
            SleepInternalLaunchState.SKIN_TEMPERATURE -> ""
            SleepInternalLaunchState.BLOOD_OXYGEN -> ""
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