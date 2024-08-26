package com.oreo.ui.sleep2.internal

import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.TrendAverage
import com.oreo.data.model.TrendsGraphData
import com.oreo.data.model.TrendsValues
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.sleep2.internal.learnmore.SleepLearnMoreDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class SleepInternalDetailsViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    val sessionManager: SessionManager,
    private val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

    var registerDate: Int = -1

    var isDeviationSelected = true

    lateinit var selectedLaunchMode: SleepInternalLaunchState

    var topContentDataAverage: TrendAverage? = null
    val topContentData = MutableLiveData<TopContentData>()

    val reloadFragment = MutableLiveData<Event<SleepInternalLaunchState>>()

    var startDate: String = ""
    var endDate: String = ""


    var selectedDate: LocalDate = LocalDate.now()

    private val _selectedPeriod = MutableLiveData<InternalSelectedPeriod>()
    val selectedPeriod: LiveData<InternalSelectedPeriod> = _selectedPeriod


    val currentFragment = MutableLiveData<Fragment>()
    var currentStartDate: LocalDate? = null

    fun setSelectedPeriod(selectedPeriod: InternalSelectedPeriod) {
        _selectedPeriod.value = selectedPeriod
    }

    private val _titleUpdate = MutableLiveData<Pair<String, Int>>()
    val titleUpdate: LiveData<Pair<String, Int>> = _titleUpdate


    val trendsData = HashMap<LocalDate, TrendsValues>()
    var currentSelectedStartDate = LocalDate.now()
    var currentSelectedEndDate = LocalDate.now()
    var currentSelectedDate = LocalDate.now()


    init {
        val datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        endDate = LocalDate.now().format(datePattern)
        startDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
            .format(datePattern)
    }


    fun getTrendsDailyData(sDate: LocalDate, eDate: LocalDate) {
        currentSelectedStartDate = sDate
        currentSelectedEndDate = eDate

        viewModelScope.launch(Dispatchers.IO) {
            userActivityRepository.getDailyTrendsData(
                sDate.toString(), eDate.toString(), selectedLaunchMode.key.lowercase()
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
                                        getTrendsDailyData(sDate, eDate)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            hasData = true
                            trendsData.clear()

                            it.data?.forEach {
                                trendsData[LocalDate.parse(it.date)] = it
                            }
                            val firstDate = it.data?.firstOrNull()
                            val avg = firstDate?.avg
                            val nudge = firstDate?.nudge

                            generateFragment(trendsData, TrendAverage(avg = avg, nudge = nudge))
                        }
                    }
                }
            }
        }

    }

    fun loadGraphData(loadPrev: Boolean) {
        if (selectedLaunchMode == SleepInternalLaunchState.SKIN_TEMPERATURE && isDeviationSelected) {
            getTrendsInternalDetailsData(LocalDate.parse(startDate), LocalDate.now())
            return
        }


        if (isHealthMonitorTrend() && selectedPeriod.value == InternalSelectedPeriod.DAILY) {
            getStartAndEndTime(loadPrev)?.let {
                getTrendsDailyData(it.first, it.second)
            }
        } else {
            getStartAndEndTime(loadPrev)?.let {
                getTrendsInternalDetailsData(it.first, it.second)
            }
        }
    }

    fun getTrendsInternalDetailsData(startDate: LocalDate, endDate: LocalDate) {
        currentSelectedStartDate = startDate
        currentSelectedEndDate = endDate

        val period =
            if (selectedLaunchMode == SleepInternalLaunchState.SKIN_TEMPERATURE && (selectedPeriod.value == InternalSelectedPeriod.DAILY || selectedPeriod.value == InternalSelectedPeriod.DAY)) {
                InternalSelectedPeriod.DAY.name.lowercase()
            } else {
                selectedPeriod.value?.name?.lowercase()
            }

        viewModelScope.launch(Dispatchers.IO) {
            if (isHealthMonitorTrend()) {
                userActivityRepository.getSleepHealthMonitorTrendsPagesData(
                    startDate.toString(),
                    getCalculatedEnd(endDate).toString(),
                    selectedLaunchMode.key.lowercase(),
                    period
                )
            } else {
                userActivityRepository.getSleepInternalTrendsPagesData(
                    startDate.toString(),
                    getCalculatedEnd(endDate).toString(),
                    selectedLaunchMode.key.lowercase(),
                    selectedPeriod.value?.name?.lowercase()
                )
            }.collect { resource ->
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
                                        getTrendsInternalDetailsData(startDate, endDate)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            hasData = true
                            trendsData.clear()

                            it.data?.forEach {
                                trendsData[LocalDate.parse(it.date)] = it
                            }

                            generateFragment(trendsData, it.avg)

                            return@collect
                        }
                    }
                }
            }
        }
    }

    private fun generateFragment(
        trends: HashMap<LocalDate, TrendsValues>, avgValue: TrendAverage?
    ) {
        val dataToDisplay = ArrayList<TrendsValues>()

        var current = currentSelectedStartDate
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        while (current!! <= currentSelectedEndDate!!) {

            val data = trends[current]
            dataToDisplay.add(
                TrendsValues(
                    date = current.format(dateFormat),
                    value1 = data?.value1,
                    value2 = data?.value2,
                    breakup = data?.breakup,
                    start_time = data?.start_time,
                    end_time = data?.end_time,
                    master_start_time = data?.master_start_time,
                    master_end_time = data?.master_end_time,
                    master_mid_time = data?.master_mid_time
                )
            )
            current = current.plusDays(1)
        }


        val trendData = TrendsGraphData(
            data = dataToDisplay, avgValue = avgValue?.avg
        )

        getFragmentToAdd(trendData).let {
            currentFragment.postValue(it)
            topContentDataAverage = avgValue

            topContentData.postValue(
                TopContentData(
                    isInteracting = false, trendsData = avgValue
                )
            )
        }
    }


    fun isHealthMonitorTrend(): Boolean {
        val healthTrends = arrayListOf(
            SleepInternalLaunchState.RESPIRATORY_RATE,
            SleepInternalLaunchState.BLOOD_OXYGEN,
            SleepInternalLaunchState.HRV,
            SleepInternalLaunchState.RESTING_HEART_RATE,
            SleepInternalLaunchState.SKIN_TEMPERATURE
        )
        return selectedLaunchMode in healthTrends
    }

    private fun getFragmentToAdd(trendData: TrendsGraphData): Fragment {
        trendData.contributorType = selectedLaunchMode

        if (selectedLaunchMode == SleepInternalLaunchState.SKIN_TEMPERATURE && isDeviationSelected) {
            return SleepTempDeviationChartFragment.newInstance(
                trendData
            )
        }

        when (selectedPeriod.value) {
            InternalSelectedPeriod.DAY, null -> {
                return when (selectedLaunchMode) {
                    SleepInternalLaunchState.REM_SLEEP, SleepInternalLaunchState.DEEP_SLEEP, SleepInternalLaunchState.RESPIRATORY_RATE, SleepInternalLaunchState.BLOOD_OXYGEN, SleepInternalLaunchState.LATENCY, SleepInternalLaunchState.RESTFULNESS, SleepInternalLaunchState.SLEEP_PERFORMANCE -> SleepBarChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.SLEEP_DURATION, SleepInternalLaunchState.HRV, SleepInternalLaunchState.RESTING_HEART_RATE, SleepInternalLaunchState.SKIN_TEMPERATURE, SleepInternalLaunchState.EFFICIENCY -> SleepSingleLineGradientChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.RESTORATIVE_SLEEP -> SleepMultiBarChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.HOUR_VS_NEED -> SleepMultiLineChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.SLEEP_TIME -> SleepTimeChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.TIMING -> SleepTimingGraphFragment.newInstance(
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

                    SleepInternalLaunchState.SLEEP_TIME, SleepInternalLaunchState.TIMING -> {
                        SleepSleepTImeChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.WEEK
                        })
                    }

                    SleepInternalLaunchState.RESPIRATORY_RATE, SleepInternalLaunchState.RESTING_HEART_RATE, SleepInternalLaunchState.BLOOD_OXYGEN, SleepInternalLaunchState.SKIN_TEMPERATURE, SleepInternalLaunchState.HRV -> {
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

                    SleepInternalLaunchState.SLEEP_TIME, SleepInternalLaunchState.TIMING -> {
                        SleepSleepTImeChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.MONTH
                        })
                    }

                    SleepInternalLaunchState.RESPIRATORY_RATE, SleepInternalLaunchState.RESTING_HEART_RATE, SleepInternalLaunchState.BLOOD_OXYGEN, SleepInternalLaunchState.SKIN_TEMPERATURE, SleepInternalLaunchState.HRV -> {
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

            InternalSelectedPeriod.DAILY -> {
                return SleepDailyGradientChartFragment.newInstance(
                    trendData
                )
            }
        }
    }

    /*
            * 0-up
            * 1-warning
            * 2-red alert
            * */
    fun getHighlightBackType(type: Int): Pair<Int, Int> {
        val background: Int
        val textColor: Int
        when (type) {
            0 -> {
                background = R.drawable.back_hm_optimal
                textColor = Color.parseColor("#29cc74")
            }

            1 -> {
                background = R.drawable.back_hm_fair
                textColor = Color.parseColor("#ffffff")
            }

            else -> {
                background = R.drawable.back_hm_warning
                textColor = Color.parseColor("#ff7c94")
            }
        }
        return Pair(background, textColor)
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
                resourcesProvider.getString(R.string.text_hrv), R.drawable.ic_hrv
            )

            SleepInternalLaunchState.SKIN_TEMPERATURE -> Pair(
                resourcesProvider.getString(R.string.text_skin_temperature),
                R.drawable.ic_skin_tempreature
            )

            SleepInternalLaunchState.BLOOD_OXYGEN -> Pair(
                resourcesProvider.getString(R.string.text_blood_oxygen), R.drawable.ic_blood_oxygen
            )
        }
    }

    fun updateTitle() {
        _titleUpdate.postValue(getTitle())
    }

    fun getLearnMoreData(): ArrayList<SleepLearnMoreDataModel> {
        val dataList = ArrayList<SleepLearnMoreDataModel>()
        when (selectedLaunchMode) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.SLEEP_PERFORMANCE -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.HOUR_VS_NEED -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.SLEEP_TIME -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.TIMING -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.EFFICIENCY -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.REM_SLEEP -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.DEEP_SLEEP -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.SLEEP_DURATION -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "**Introduction**  \n" +
                                "Sleep duration is a critical component of overall health and well-being. It refers to the total amount of sleep an individual gets each night and plays a crucial role in various bodily functions, including cognitive performance, physical recovery, and emotional regulation. Understanding the concept of sleep duration and how it varies across different stages of life is essential for optimizing health.\n" +
                                "\n" +
                                "**The basics of sleep duration**  \n" +
                                "Sleep duration is the amount of time spent asleep from the moment you fall asleep until you wake up. This period encompasses all stages of sleep, including light sleep, deep sleep, and REM (rapid eye movement) sleep, each of which is vital for different aspects of physical and mental recovery.\n" +
                                "\n" +
                                "**Why sleep duration matters**  \n" +
                                "Adequate sleep duration is necessary for the body to repair tissues, regulate hormones, and consolidate memories. Chronic sleep deprivation, where an individual consistently gets less sleep than needed, can lead to various health issues such as weakened immune function, increased risk of chronic diseases, impaired cognitive function, and mood disturbances.\n" +
                                "\n" +
                                "**Sleep duration across different age groups**  \n" +
                                "Sleep needs are not static and change throughout the human lifespan.\n" +
                                "\n" +
                                "• **Infants and young children:** In the early stages of life, sleep is essential for growth and development. Infants typically require 14 to 17 hours of sleep, which gradually decreases as they grow older. By the time children reach school age, they generally need 9 to 11 hours of sleep.  \n" +
                                "• **Teenagers:** Adolescents need about 8 to 10 hours of sleep to support their rapid physical and cognitive development. This stage often sees a shift in sleep patterns, with teenagers naturally inclined to stay up later, which can conflict with early school start times.  \n" +
                                "• **Adults:** Most adults function best with 7 to 9 hours of sleep. This range helps maintain optimal cognitive performance, emotional stability, and physical health.  \n" +
                                "• **Older adults:** Although the amount of sleep needed doesn’t drastically change in older age, sleep patterns can shift. Older adults may find it more challenging to achieve continuous, deep sleep, but it remains important to aim for 7 to 8 hours to support overall health.  \n" +
                                "\n" +
                                "**Factors affecting sleep duration**  \n" +
                                "Several factors can influence how much sleep you get, including lifestyle choices, sleep environment, and health conditions. Stress, caffeine, screen time, and irregular sleep schedules can all negatively impact sleep duration. Understanding these factors and how they affect your sleep can help you make adjustments to improve your sleep quality and duration.",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.LATENCY -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.RESTFULNESS -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.RESPIRATORY_RATE -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.RESTING_HEART_RATE -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.HRV -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.SKIN_TEMPERATURE -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }

            SleepInternalLaunchState.BLOOD_OXYGEN -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )
            }
        }

        return dataList
    }

    fun reloadData() {
        currentStartDate = null
        trendsData.clear()

        hasData = false
        loadGraphData(false)
    }

    fun getUnit(): String {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> ""
            SleepInternalLaunchState.SLEEP_PERFORMANCE -> "%"
            SleepInternalLaunchState.HOUR_VS_NEED -> ""
            SleepInternalLaunchState.SLEEP_TIME -> ""
            SleepInternalLaunchState.TIMING -> ""
            SleepInternalLaunchState.EFFICIENCY -> "%"
            SleepInternalLaunchState.REM_SLEEP -> "min"
            SleepInternalLaunchState.DEEP_SLEEP -> "min"
            SleepInternalLaunchState.SLEEP_DURATION -> ""
            SleepInternalLaunchState.LATENCY -> "min"
            SleepInternalLaunchState.RESTFULNESS -> "times"
            SleepInternalLaunchState.RESPIRATORY_RATE -> "rpm"
            SleepInternalLaunchState.RESTING_HEART_RATE -> "bpm"
            SleepInternalLaunchState.HRV -> "ms"
            SleepInternalLaunchState.SKIN_TEMPERATURE -> {
                if (sessionManager.isMetric()) {
                    "°C"
                } else {
                    "°F"
                }
            }

            SleepInternalLaunchState.BLOOD_OXYGEN -> "%"
        }
    }

    fun getTopState(interacting: Boolean): TrendsTopState {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.SLEEP_DURATION, SleepInternalLaunchState.SLEEP_TIME -> {
                TrendsTopState.SINGLE_DATE
            }

            SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                if (interacting) {
                    TrendsTopState.DOUBLE_DATE
                } else {
                    TrendsTopState.SINGLE_DATE
                }
            }

            SleepInternalLaunchState.HOUR_VS_NEED -> {
                TrendsTopState.DOUBLE_DATE
            }

            else -> {
                TrendsTopState.SINGLE
            }
        }
    }

    fun loadPreviousPeriodData() {
        loadGraphData(true)

    }

    fun loadNextPeriodData() {
        loadGraphData(false)
    }

    private fun getCalculatedEnd(endOfWeek: LocalDate): LocalDate {

        val todayDate = LocalDate.now()
        var calculatedEndDate = endOfWeek

        if (endOfWeek > todayDate) {
            calculatedEndDate = todayDate
        }
        return calculatedEndDate
    }


    var hasData = false
    fun getStartAndEndTime(loadPrev: Boolean): Pair<LocalDate, LocalDate>? {
        return when (selectedPeriod.value) {
            InternalSelectedPeriod.DAILY -> {
                if (hasData) {
                    if (loadPrev) {
                        val prevDay = currentSelectedStartDate.minusDays(1)
                        Pair(prevDay, prevDay)
                    } else {
                        val nextStartDate = currentSelectedStartDate.plusDays(1)
                        if (nextStartDate > LocalDate.now()) {
                            return null
                        }
                        Pair(nextStartDate, nextStartDate)
                    }
                } else {
                    Pair(selectedDate, selectedDate)
                }

            }

            InternalSelectedPeriod.DAY, null -> {
                if (hasData) {
                    if (loadPrev) {
                        //todo handle user data end
                        val startOfWeek = currentSelectedStartDate.with(
                            TemporalAdjusters.previous(
                                DayOfWeek.MONDAY
                            )
                        )

                        val endOfWeek = currentSelectedEndDate.with(
                            TemporalAdjusters.previous(
                                DayOfWeek.SUNDAY
                            )
                        )
                        Pair(startOfWeek, endOfWeek)
                    } else {
                        val startOfWeek = currentSelectedStartDate.with(
                            TemporalAdjusters.next(
                                DayOfWeek.MONDAY
                            )
                        )

                        val endOfWeek = currentSelectedEndDate.with(
                            TemporalAdjusters.next(
                                DayOfWeek.SUNDAY
                            )
                        )

                        if (startOfWeek > LocalDate.now()) {
                            return null
                        }
                        Pair(startOfWeek, endOfWeek)
                    }

                } else {
                    val startOfWeek = selectedDate.with(
                        TemporalAdjusters.previousOrSame(
                            DayOfWeek.MONDAY
                        )
                    )

                    val endOfWeek = selectedDate.with(
                        TemporalAdjusters.nextOrSame(
                            DayOfWeek.SUNDAY
                        )
                    )
                    Pair(startOfWeek, endOfWeek)
                }
            }

            InternalSelectedPeriod.WEEK -> {
                if (hasData) {
                    if (loadPrev) {
                        //todo handle user data end
                        val startOfWeek = currentSelectedStartDate.with(
                            TemporalAdjusters.previous(
                                DayOfWeek.MONDAY
                            )
                        ).minusWeeks(5)

                        val endOfWeek = currentSelectedEndDate.with(
                            TemporalAdjusters.previous(
                                DayOfWeek.SUNDAY
                            )
                        ).minusWeeks(5)

                        Pair(startOfWeek, endOfWeek)
                    } else {
                        val startOfWeek = currentSelectedStartDate.with(
                            TemporalAdjusters.next(
                                DayOfWeek.MONDAY
                            )
                        ).plusWeeks(5)

                        val endOfWeek = currentSelectedEndDate.with(
                            TemporalAdjusters.next(
                                DayOfWeek.SUNDAY
                            )
                        ).plusWeeks(5)

                        if (startOfWeek > LocalDate.now()) {
                            return null
                        }
                        Pair(startOfWeek, endOfWeek)
                    }

                } else {

                    val endOfWeek = selectedDate.with(
                        TemporalAdjusters.nextOrSame(
                            DayOfWeek.SUNDAY
                        )
                    )
                    val startOfWeek =
                        endOfWeek.minusWeeks(5).with(TemporalAdjusters.previous(DayOfWeek.MONDAY))

                    Pair(startOfWeek, endOfWeek)
                }
            }

            InternalSelectedPeriod.MONTH -> {
                if (hasData) {
                    if (loadPrev) {
                        //todo handle user data end
                        val start = currentSelectedStartDate!!.minusMonths(6)
                            .with(TemporalAdjusters.firstDayOfMonth())
                        val end = currentSelectedStartDate!!.minusMonths(1)
                            .with(TemporalAdjusters.lastDayOfMonth())

                        Pair(start, end)
                    } else {
                        val start = currentSelectedEndDate!!.plusMonths(1)
                            .with(TemporalAdjusters.firstDayOfMonth())
                        val end = start.plusMonths(5).with(TemporalAdjusters.lastDayOfMonth())

                        Pair(start, end)

                        if (start > LocalDate.now()) {
                            return null
                        }
                        Pair(start, end)
                    }

                } else {
                    val start =
                        selectedDate.minusMonths(5).with(TemporalAdjusters.firstDayOfMonth())
                    val end = selectedDate.with(TemporalAdjusters.lastDayOfMonth())
                    Pair(start, end)
                }
            }
        }
    }

    fun getDuration(trendsValues: TrendsValues?): Float? {
        val startDate = trendsValues?.master_start_time
        val endDate = trendsValues?.master_end_time
        if (startDate == null || endDate == null) {
            return null
        } else {
            val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return Duration.between(
                LocalDateTime.parse(startDate, format), LocalDateTime.parse(endDate, format)
            ).toSeconds().toFloat()
        }
    }

    fun getDisplayDate(): String {
        val format = DateTimeFormatter.ofPattern("dd MMM")
        return if (currentSelectedStartDate == currentSelectedEndDate) {
            currentSelectedStartDate.format(format)

        } else {
            "${currentSelectedStartDate.format(format)} - ${
                currentSelectedEndDate.format(
                    format
                )
            }"
        }
    }

    fun handle255(value: Float?, selectedLaunchMode: SleepInternalLaunchState): Float? {
        return if (selectedLaunchMode == SleepInternalLaunchState.RESPIRATORY_RATE || selectedLaunchMode == SleepInternalLaunchState.RESTING_HEART_RATE || selectedLaunchMode == SleepInternalLaunchState.HRV) {
            if (value == 255f) null else value
        } else {
            value
        }
    }

    fun showCalibrating(): Boolean {
        return selectedLaunchMode == SleepInternalLaunchState.SKIN_TEMPERATURE && isDeviationSelected && registerDate <= 7
    }


}

data class TopContentData(
    val isInteracting: Boolean,
    val date: LocalDate? = null,
    val trendsData: TrendAverage? = null,
    val dailyValue: Float? = null,
)

enum class InternalSelectedPeriod {
    DAILY, DAY, WEEK, MONTH
}

enum class TrendsTopState {
    SINGLE, SINGLE_DATE, DOUBLE_DATE
}