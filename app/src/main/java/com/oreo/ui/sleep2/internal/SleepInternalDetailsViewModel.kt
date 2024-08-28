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
                        toolbarTitle = "Restorative sleep",
                        title = "Understanding restorative sleep",
                        content = "**Introduction**\n" +
                                "\n" +
                                "Restorative sleep is a crucial part of your sleep cycle, made up of deep sleep and REM sleep. These two stages are essential for physical recovery, mental clarity, and emotional well-being. Ideally, you should spend 40-50% of your sleep in restorative sleep to wake up feeling refreshed and ready to take on the day. Understanding what restorative sleep is and why it matters can help you prioritize your sleep health.\n" +
                                "\n" +
                                "**What is restorative sleep?**\n" +
                                "\n" +
                                "Restorative sleep combines two critical stages of sleep: deep sleep and REM sleep. Deep sleep is the stage where your body focuses on repairing muscles, building bones, and boosting your immune system. REM sleep, on the other hand, is when your brain processes memories, emotions, and information. Together, these stages help restore your body and mind, making you feel rejuvenated after a good night’s sleep.\n" +
                                "\n" +
                                "**Why you need restorative sleep** ?\n" +
                                "\n" +
                                "Spending 40-50% of your sleep in restorative sleep is important because:\n" +
                                "\n" +
                                "*   **Physical recovery:** Deep sleep allows your body to heal and grow. It’s when most of your body’s repair processes happen, from muscle recovery to tissue growth.\n" +
                                "    \n" +
                                "*   **Mental health:** REM sleep helps your brain process emotions and memories, playing a key role in mental and emotional health.\n" +
                                "    \n" +
                                "*   **Cognitive function:** Restorative sleep improves your ability to think clearly, learn new information, and solve problems. Without enough restorative sleep, you may feel foggy and find it harder to concentrate.\n" +
                                "    \n" +
                                "\n" +
                                "**How to achieve more restorative sleep?**\n" +
                                "\n" +
                                "To ensure you’re getting enough restorative sleep, try these tips:\n" +
                                "\n" +
                                "*   **Maintain a consistent sleep schedule:** Go to bed and wake up at the same time every day to help regulate your sleep cycle.\n" +
                                "    \n" +
                                "*   **Create a relaxing bedtime routine:** Engage in calming activities before bed, like reading or taking a warm bath, to prepare your body for sleep.\n" +
                                "    \n" +
                                "*   **Limit caffeine and alcohol:** Avoid consuming caffeine and alcohol close to bedtime, as they can disrupt your sleep cycle and reduce restorative sleep.\n" +
                                "    \n" +
                                "*   **Optimize your sleep environment:** Make sure your bedroom is dark, quiet, and cool to promote uninterrupted sleep.\n",
                        img = R.drawable.image_learn_common,
                        internalImg = R.drawable.image_learn_common_int
                    )
                )
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Restorative sleep",
                        title = "The benefits of getting enough restorative sleep",
                        content = "**Introduction**\n\n Restorative sleep, which includes deep sleep and REM sleep, is the most vital part of your sleep cycle. To feel truly rested, you should aim to spend 40-50% of your sleep in these stages. Getting enough restorative sleep offers numerous benefits for your body and mind, helping you maintain optimal health.\n" +
                                "\n" +
                                "**Why restorative sleep is key?**\n\nRestorative sleep is where the magic happens during your sleep cycle. It’s when your body heals and your mind recharges. Here’s why it’s important:\n" +
                                "\n" +
                                "*   **Physical health:** During deep sleep, your body repairs tissues, builds muscle, and strengthens your immune system. This stage of sleep is critical for physical recovery, especially if you’re active or recovering from illness.\n" +
                                "    \n" +
                                "*   **Mental clarity:** REM sleep is when your brain sorts through information, consolidating memories and processing emotions. This is essential for learning and mental sharpness.\n" +
                                "    \n" +
                                "*   **Emotional stability:** Proper amounts of restorative sleep help regulate your mood, reducing the likelihood of anxiety and depression.\n" +
                                "    \n" +
                                "\n" +
                                "**Benefits of spending 40-50% of your sleep in restorative sleep**\n" +
                                "\n" +
                                "1.  **Enhanced physical recovery:** The more time you spend in deep sleep, the better your body can recover from physical exertion. This is especially important for athletes and anyone who exercises regularly.\n" +
                                "    \n" +
                                "2.  **Improved memory and learning :** REM sleep plays a crucial role in memory consolidation. Spending enough time in this stage helps you retain new information and perform better in tasks that require focus and concentration.\n" +
                                "    \n" +
                                "3.  **Better mood and mental health:** Adequate restorative sleep helps keep your mood stable and your mental health in check. It reduces stress, lowers the risk of mood disorders, and helps you manage emotions more effectively.\n" +
                                "    \n" +
                                "4.  **Stronger immune system:** Deep sleep boosts your immune system by promoting the production of immune cells that fight off infections. Getting enough restorative sleep helps protect your body from illness.\n" +
                                "    \n" +
                                "\n" +
                                "**Tips for maximizing restorative sleep**\n" +
                                "\n" +
                                "*   **Stick to a sleep schedule:** Consistency helps your body get into a rhythm, making it easier to achieve the right balance of deep sleep and REM sleep.\n" +
                                "    \n" +
                                "*   **Create a comfortable sleep environment:** Ensure your bedroom is conducive to sleep by keeping it dark, quiet, and cool.\n" +
                                "    \n" +
                                "*   **Avoid stimulants before bed:** Caffeine and nicotine can interfere with your ability to reach deep sleep and REM sleep, so it’s best to avoid them in the evening.\n",
                        img = R.drawable.image_learn_restorative,
                        internalImg = R.drawable.image_learn_restorative_int
                    )
                )
            }

            SleepInternalLaunchState.SLEEP_PERFORMANCE -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep performance",
                        title = "What is sleep performance?",
                        content = "Understanding how well you sleep each night can give you valuable insights into your overall health. Let's explore what sleep performance is, why it matters, and how you can measure it.\n" +
                                "\n" +
                                "Sleep performance is a measure of how well you sleep, expressed as a percentage. It compares the actual hours you’ve slept to the hours you should have slept based on your sleep need. Ideally, your sleep performance should be above 70%.\n" +
                                "\n" +
                                "**Optimal ranges**\n" +
                                "\n" +
                                "Aim for a sleep performance above 70% to ensure you’re getting enough quality sleep.\n" +
                                "\n" +
                                "**Benefits of measuring sleep performance**\n" +
                                "\n" +
                                "Tracking your sleep performance can lead to numerous benefits:\n" +
                                "\n" +
                                "*   **Improved physical health**\n" +
                                "    \n" +
                                "    *   Enhanced immune function.\n" +
                                "        \n" +
                                "    *   Better muscle recovery and growth.\n" +
                                "        \n" +
                                "    *   Balanced hormone levels.\n" +
                                "        \n" +
                                "*   **Better mental health**\n" +
                                "    \n" +
                                "    *   Improved mood regulation.\n" +
                                "        \n" +
                                "    *   Reduced stress and anxiety.\n" +
                                "        \n" +
                                "    *   Enhanced cognitive function and memory.\n" +
                                "        \n" +
                                "*   **Overall well-being**\n" +
                                "    \n" +
                                "    *   Increased energy levels.\n" +
                                "        \n" +
                                "    *   Better productivity and focus.\n" +
                                "        \n" +
                                "    *   Improved quality of life.\n" +
                                "        \n" +
                                "\n" +
                                "**How to measure sleep performance?**\n" +
                                "\n" +
                                "*   **Use sleep tracking technology**\n" +
                                "    \n" +
                                "    *   Devices like smart rings, watches, and apps can provide detailed insights into your sleep performance.\n" +
                                "        \n" +
                                "    *   These tools can automatically calculate your sleep performance percentage based on your sleep hours and need.\n" +
                                "        \n" +
                                "*   **Keep a sleep diary**\n" +
                                "    \n" +
                                "    *   Manually track your sleep patterns by noting when you go to bed, wake up, and any naps you take during the day.\n" +
                                "        \n" +
                                "    *   Calculate your sleep performance percentage using the formula provided.\n" +
                                "        \n" +
                                "*   **Consult with a sleep specialist**\n" +
                                "    \n" +
                                "    *   If you’re having persistent sleep issues, a professional can help diagnose and treat any underlying conditions.\n" +
                                "        \n" +
                                "    *   They can also provide personalized advice on improving your sleep performance.\n" +
                                "        \n" +
                                "\n" +
                                "**Conclusion**\n" +
                                "\n" +
                                "Measuring your sleep performance is the first step to better sleep. By understanding your sleep performance percentage and making informed decisions, you can improve your sleep quality and overall health. Start tracking your sleep performance and take control of your sleep health!\n",
                        img = R.drawable.image_learn_common,
                        internalImg = R.drawable.image_learn_common_int
                    )
                )
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep performance",
                        title = "How to improve sleep performance?",
                        content = "Improving your sleep performance can lead to better health and well-being. Let's explore how you can enhance your sleep performance to feel your best every day.\n" +
                                "\n" +
                                "**Why improve sleep performance?**\n\nIncreasing the percentage of sleep hours you get relative to your sleep need can enhance your physical health, mental well-being, and overall quality of life.\n" +
                                "\n" +
                                "Tips for improving sleep performance\n" +
                                "\n" +
                                "1.  **Optimize your sleep environment**\n" +
                                "    \n" +
                                "    *   **Tip**: Keep your bedroom cool, dark, and quiet. Consider using blackout curtains, white noise machines, or earplugs if needed.\n" +
                                "        \n" +
                                "    *   **Benefit**: Reduces sleep disturbances and promotes deeper, more restful sleep.\n" +
                                "        \n" +
                                "2.  **Establish a regular sleep routine**\n" +
                                "    \n" +
                                "    *   **Tip**: Go to bed and wake up at the same time every day, even on weekends. This helps regulate your internal clock.\n" +
                                "        \n" +
                                "    *   **Benefit**: Consistent sleep patterns improve sleep quality and performance.\n" +
                                "        \n" +
                                "3.  **Practice good sleep hygiene**\n" +
                                "    \n" +
                                "    *   **Tip**: Avoid caffeine and heavy meals before bedtime. Create a relaxing pre-sleep routine, like reading or taking a warm bath.\n" +
                                "        \n" +
                                "    *   **Benefit**: Minimizes disruptions to melatonin production and digestive discomfort that can interfere with sleep.\n" +
                                "        \n" +
                                "4.  **Manage stress effectively**\n" +
                                "    \n" +
                                "    *   **Tip**: Engage in relaxation techniques such as meditation, deep breathing exercises, or gentle yoga to reduce stress before bed.\n" +
                                "        \n" +
                                "    *   **Benefit**: Reduces cortisol levels and promotes relaxation, making it easier to fall asleep and stay asleep.\n" +
                                "        \n" +
                                "5.  **Limit exposure to screens**\n" +
                                "    \n" +
                                "    *   **Tip**: Reduce screen time at least an hour before bed. The blue light from screens can interfere with your body’s production of melatonin, a hormone that regulates sleep.\n" +
                                "        \n" +
                                "    *   **Benefit**: Helps maintain natural sleep-wake cycles and improves sleep quality.\n" +
                                "        \n" +
                                "6.  **Stay active**\n" +
                                "    \n" +
                                "    *   **Tip**: Regular physical activity can help you fall asleep faster and enjoy deeper sleep. Just avoid vigorous exercise close to bedtime.\n" +
                                "        \n" +
                                "    *   **Benefit**: Physical activity helps regulate sleep patterns and improve overall sleep quality.\n" +
                                "        \n" +
                                "7.  **Use sleep technology**\n" +
                                "    \n" +
                                "    *   **Tip**: Utilize sleep trackers to monitor your sleep stages and identify patterns. Use the data to make adjustments to your habits for better sleep performance.\n" +
                                "        \n" +
                                "    *   **Benefit**: Personalized insights can help you make informed decisions to improve your sleep.\n" +
                                "        \n" +
                                "\n" +
                                "**Conclusion**\n\nImproving sleep performance is about making small, consistent changes to your routine and environment. By optimizing your sleep habits and focusing on getting the right amount of sleep relative to your need, you can enjoy better health, improved mood, and higher productivity.\n",
                        img = R.drawable.image_learn_performance,
                        internalImg = R.drawable.image_learn_performance_int
                    )
                )
            }

            SleepInternalLaunchState.HOUR_VS_NEED -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Hours VS Need",
                        title = "The science of hours vs need",
                        content = "**Understanding sleep needs**\n" +
                                "\n" +
                                "Ever wondered why some people seem to need more sleep than others? Let's explore the concept of sleep needs and how they vary by age and individual factors.\n" +
                                "\n" +
                                "**What is sleep need?**\n" +
                                "\n" +
                                "Sleep need refers to the amount of sleep an individual requires to function optimally. This need varies based on several factors, including age, lifestyle, and genetics. It is not a fixed number but is calculated daily, taking into account:\n" +
                                "\n" +
                                "*   **Personalized baseline:** This is the average amount of sleep your body typically needs to feel rested. It's unique to each person. On an average, an adult needs a minimum of 7 hours of sleep.\n" +
                                "    \n" +
                                "*   **Sleep debt:** This accumulates when you don't get enough sleep over a period of time. For example, if your body needs 8 hours of sleep but you only get 6 hours, you accumulate 2 hours of sleep debt.\n" +
                                "    \n" +
                                "*   **Previous day's activity:** Physical and mental activities from the previous day impact how much rest your body needs to recover. More strenuous activities may increase your sleep need.\n" +
                                "    \n" +
                                "*   **Naps:** Short sleep periods (less than 3 hours) during the day can affect your overall sleep need for the night. They can help reduce sleep debt but may also influence night-time sleep patterns.\n" +
                                "    \n" +
                                "\n" +
                                "In essence, sleep need is a measure of how much sleep you need to be fully rested and ready for the next day.\n" +
                                "\n" +
                                "**What are sleep hours?**\n" +
                                "\n" +
                                "Sleep hours refer to the actual amount of time you spend sleeping, including naps. This is the total sleep duration you get in a 24-hour period. It's a straightforward measure of how many hours you've actually slept, regardless of how much sleep you need.\n" +
                                "\n" +
                                "**Hours vs Need: the balance**\n" +
                                "\n" +
                                "Balancing sleep hours and sleep need is crucial for optimal functioning. Here’s how it works:\n" +
                                "\n" +
                                "*   **Matching hours to need:** Ideally, the number of sleep hours should meet your calculated sleep need. For instance, if your sleep need is 8 hours but you only sleep for 6 hours, you’ll likely feel tired and accumulate sleep debt.\n" +
                                "    \n" +
                                "*   **Exceeding sleep need:** Occasionally, sleeping more than your need can help repay accumulated sleep debt. However, consistently oversleeping may indicate underlying health issues.\n" +
                                "    \n" +
                                "*   **Falling short of sleep need:** When you consistently get less sleep than your body needs, it leads to sleep debt. Over time, this can affect your physical and mental health, leading to issues like fatigue, decreased cognitive function, and weakened immune response.\n",
                        img = R.drawable.image_learn_common,
                        internalImg = R.drawable.image_learn_common_int
                    )
                )
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Hours VS Need",
                        title = "Why does sleep need matters and how to improve it?",
                        content = "Let’s delve into the science behind sleep need, why you might be falling short, the consequences of not meeting your sleep need, and practical ways to improve your sleep.\n" +
                                "\n" +
                                "**Why you might not be meeting your sleep need**\n" +
                                "\n" +
                                "Several factors can prevent you from getting the sleep your body requires:\n" +
                                "\n" +
                                "*   **Stress and anxiety**\n" +
                                "    \n" +
                                "    *   **Impact**: Stress and anxiety can make it difficult to fall asleep and stay asleep, reducing your overall sleep quality and duration.\n" +
                                "        \n" +
                                "    *   **Science**: Stress activates the hypothalamic-pituitary-adrenal (hpa) axis, increasing cortisol levels and making it harder to relax and fall asleep.\n" +
                                "        \n" +
                                "*   **Poor sleep hygiene**\n" +
                                "    \n" +
                                "    *   **Impact**: Inconsistent sleep schedules, exposure to screens before bedtime, and an uncomfortable sleep environment can interfere with your ability to fall asleep.\n" +
                                "        \n" +
                                "    *   **Science**: Blue light from screens suppresses melatonin production, a hormone crucial for regulating sleep-wake cycles.\n" +
                                "        \n" +
                                "*   **Lifestyle factors**\n" +
                                "    \n" +
                                "    *   **Impact**: Irregular work hours, frequent travel, and lack of physical activity can disrupt your sleep patterns.\n" +
                                "        \n" +
                                "    *   **Science**: Irregular sleep schedules can disrupt circadian rhythms, the body’s internal clock that regulates the sleep-wake cycle.\n" +
                                "        \n" +
                                "*   **Health issues**\n" +
                                "    \n" +
                                "    *   **Impact**: Conditions like sleep apnea, chronic pain, and other medical issues can significantly disrupt sleep.\n" +
                                "        \n" +
                                "    *   **Science**: Sleep disorders can cause fragmented sleep, reducing both the quantity and quality of sleep.\n" +
                                "        \n" +
                                "\n" +
                                "**Ways to improve your sleep and meet your sleep need:**\n" +
                                "\n" +
                                "*   **Establish a consistent sleep schedule**\n" +
                                "    \n" +
                                "    *   **Tip**: Go to bed and wake up at the same time every day, even on weekends.\n" +
                                "        \n" +
                                "    *   **Benefit**: Helps regulate your circadian rhythms and improve sleep quality.\n" +
                                "        \n" +
                                "*   **Create a sleep-friendly environment**\n" +
                                "    \n" +
                                "    *   **Tip**: Ensure your bedroom is cool, dark, and quiet. Invest in a comfortable mattress and pillows.\n" +
                                "        \n" +
                                "    *   **Benefit**: Reduces sleep disturbances and promotes deeper, more restful sleep.\n" +
                                "        \n" +
                                "*   **Practice good sleep hygiene**\n" +
                                "    \n" +
                                "    *   **Tip**: Avoid screens at least an hour before bed, limit caffeine and heavy meals in the evening.\n" +
                                "        \n" +
                                "    *   **Benefit**: Minimizes disruptions to melatonin production and digestive discomfort that can interfere with sleep.\n" +
                                "        \n" +
                                "*   **Manage stress and anxiety**\n" +
                                "    \n" +
                                "    *   **Tip**: Incorporate relaxation techniques such as meditation, deep breathing exercises, or yoga into your daily routine.\n" +
                                "        \n" +
                                "    *   **Benefit**: Reduces cortisol levels and promotes relaxation, making it easier to fall asleep.\n" +
                                "        \n" +
                                "*   **Get regular physical activity**\n" +
                                "    \n" +
                                "    *   **Tip**: Engage in at least 30 minutes of moderate exercise most days of the week, but avoid vigorous activity close to bedtime.\n" +
                                "        \n" +
                                "    *   **Benefit**: Helps regulate sleep patterns and improve overall sleep quality.\n" +
                                "        \n" +
                                "*   **Address health issues**\n" +
                                "    \n" +
                                "    *   **Tip**: Consult a healthcare provider if you have chronic pain, sleep apnea, or other medical conditions that affect your sleep.\n" +
                                "        \n" +
                                "    *   **Benefit**: Proper management of health issues can significantly improve sleep quality and duration.\n",
                        img = R.drawable.image_learn_hour_vs_need,
                        internalImg = R.drawable.image_learn_hour_vs_need_int
                    )
                )
            }

            SleepInternalLaunchState.SLEEP_TIME -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep consistency",
                        title = "Understanding sleep consistency and its importance",
                        content = "**Introduction**\n" +
                                "\n" +
                                "Sleep consistency refers to the regularity of your bedtime and wake-up time. Maintaining a consistent sleep schedule is crucial for overall sleep quality and health. Ideally, over a two-week period, both your bedtime and wake-up time should have a standard deviation of 60 minutes or less. This means you should be going to bed and waking up at roughly the same time every day, with minimal variation. Understanding the importance of sleep consistency can help you establish better sleep habits and improve your overall well-being.\n" +
                                "\n" +
                                "**What is sleep consistency?**\n" +
                                "\n" +
                                "Sleep consistency is about how stable your sleep and wake times are. If you go to bed at 10 p.m. one night and 1 a.m. the next, your sleep consistency is poor. The goal is to have a regular pattern where the time you go to sleep and the time you wake up don’t vary too much. Ideally, over two weeks, your bedtime and wake-up time should not vary by more than 60 minutes from day to day.\n" +
                                "\n" +
                                "**Why sleep consistency matters** ?\n" +
                                "\n" +
                                "Maintaining sleep consistency is important because it helps regulate your body’s internal clock, known as the circadian rhythm. When you go to bed and wake up at the same time every day, your body knows when to release sleep-inducing hormones like melatonin and when to prepare for wakefulness. This regularity leads to better sleep quality and more restorative rest.\n" +
                                "\n" +
                                "**Impact of inconsistent sleep patterns?**\n" +
                                "\n" +
                                "Inconsistent sleep patterns can disrupt your circadian rhythm, leading to difficulties falling asleep, staying asleep, and waking up feeling rested. Over time, poor sleep consistency can contribute to a range of health issues, including:\n" +
                                "\n" +
                                "*   **Increased stress:** Irregular sleep schedules can increase cortisol levels, the stress hormone, making it harder to relax and sleep well.\n" +
                                "    \n" +
                                "*   **Reduced cognitive function:** Inconsistent sleep can impair memory, focus, and decision-making skills.\n" +
                                "    \n" +
                                "*   **Weakened immune system:** Poor sleep consistency can weaken your immune response, making you more susceptible to illnesses.\n" +
                                "    \n" +
                                "\n" +
                                "**How to improve sleep consistency?**\n" +
                                "\n" +
                                "To achieve better sleep consistency, consider the following tips:\n" +
                                "\n" +
                                "*   **Set a regular bedtime and wake-up time:** Try to go to bed and wake up at the same time every day, even on weekends.\n" +
                                "    \n" +
                                "*   **Create a relaxing pre-sleep routine:** Engage in calming activities before bed to help signal to your body that it’s time to sleep.\n" +
                                "    \n" +
                                "*   **Avoid large variations in sleep schedule:** Try to keep your sleep and wake times within a 60-minute window from day to day.\n" +
                                "    \n" +
                                "*   **Monitor your sleep patterns:** Use a sleep tracker to keep an eye on your sleep consistency over time and make adjustments as needed.\n",
                        img = R.drawable.image_learn_common,
                        internalImg = R.drawable.image_learn_common_int
                    )
                )
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep consistency",
                        title = "The benefits of maintaining sleep consistency",
                        content = "**Introduction**\n\nSleep consistency is essential for achieving high-quality sleep and maintaining overall health. It refers to how regularly you go to bed and wake up at the same times each day. Ideally, your sleep and wake times should not vary by more than 60 minutes over a two-week period. Maintaining sleep consistency offers numerous benefits that can enhance your physical, mental, and emotional well-being.\n" +
                                "\n" +
                                "**Why sleep consistency is important** ?\n" +
                                "\n" +
                                "Your body’s internal clock, or circadian rhythm, relies on consistency. When you maintain a regular sleep schedule, your body knows when to prepare for sleep and when to wake up. This regularity helps ensure you get enough deep and REM sleep, which are crucial for recovery and cognitive function.\n" +
                                "\n" +
                                "**Benefits of maintaining sleep consistency**\n" +
                                "\n" +
                                "1.  **Improved sleep quality:** By going to bed and waking up at consistent times, your body can better regulate its sleep cycles. This leads to deeper, more restorative sleep, helping you wake up feeling refreshed and energized.\n" +
                                "    \n" +
                                "2.  **Enhanced mental clarity:** Consistent sleep patterns help improve cognitive functions like memory, focus, and problem-solving skills. When your sleep is regular, your brain has the opportunity to perform critical processes that support mental clarity.\n" +
                                "    \n" +
                                "3.  **Better mood regulation:** Sleep consistency plays a significant role in mood stability. Regular sleep patterns help regulate the production of hormones like serotonin, which affects mood. As a result, maintaining a consistent sleep schedule can reduce the risk of mood disorders such as anxiety and depression.\n" +
                                "    \n" +
                                "4.  **Strengthened immune system:** A consistent sleep schedule supports your immune system by giving your body the time it needs to repair and strengthen itself during sleep. This can lead to better overall health and a reduced risk of illness.\n" +
                                "    \n" +
                                "5.  **Reduced stress levels:** When your body knows when to expect sleep, it can better manage stress. Consistent sleep patterns help lower cortisol levels, leading to a calmer and more relaxed state throughout the day.\n" +
                                "    \n" +
                                "\n" +
                                "**How to maintain sleep consistency?**\n" +
                                "\n" +
                                "*   **Stick to a routine:** Go to bed and wake up at the same times every day, even on weekends.\n" +
                                "    \n" +
                                "*   **Prepare for sleep:** Establish a bedtime routine that includes relaxing activities to help you unwind.\n" +
                                "    \n" +
                                "*   **Avoid late-night activities:** Limit exposure to screens and bright lights before bed to help your body recognize that it’s time to sleep.\n" +
                                "    \n" +
                                "*   **Track your sleep patterns:** Use a sleep tracker to monitor your consistency and make adjustments as needed to stay within the ideal 60-minute window.\n",
                        img = R.drawable.image_learn_time,
                        internalImg = R.drawable.image_learn_time_int
                    )
                )
            }

            SleepInternalLaunchState.TIMING -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep timing",
                        title = "Understanding sleep timing",
                        content = "Sleep timing refers to when your sleep occurs within the 24-hour day, and the most crucial part of this is the midpoint of sleep. The midpoint of your sleep is the time halfway between when you fall asleep and when you wake up. For optimal health, this midpoint should ideally fall between 12 a.m. and 3 a.m. Understanding sleep timing can help you align your sleep with your body’s natural rhythms, leading to better rest and overall well-being.\n" +
                                "\n" +
                                "**What is the midpoint of sleep?**\n" +
                                "\n" +
                                "The midpoint of sleep is exactly what it sounds like—the middle of your sleep period. For example, if you go to bed at 10 p.m. and wake up at 6 a.m., your midpoint of sleep is 2 a.m. This timing is significant because it aligns with your body’s natural circadian rhythms, which regulate many of your biological processes, including sleep.\n" +
                                "\n" +
                                "**Why the midpoint of sleep matters**\n" +
                                "\n" +
                                "Having your midpoint of sleep fall between 12 a.m. and 3 a.m. is important for a few reasons:\n" +
                                "\n" +
                                "*   **Circadian rhythm alignment:** Your body’s internal clock is programmed to follow a natural cycle that is closely tied to the environment, particularly light and darkness. Sleeping with a midpoint between 12 a.m. and 3 a.m. helps keep your circadian rhythm in sync, which is crucial for good sleep quality and overall health.\n" +
                                "    \n" +
                                "*   **Better sleep quality:** When your sleep is aligned with your circadian rhythm, you’re more likely to experience deep, restorative sleep stages, which are essential for physical and mental recovery.\n" +
                                "    \n" +
                                "*   **Health benefits:** Proper sleep timing has been linked to a lower risk of developing chronic conditions such as obesity, diabetes, and cardiovascular disease.\n" +
                                "    \n" +
                                "\n" +
                                "**How to achieve the ideal midpoint of sleep**\n" +
                                "\n" +
                                "Here’s how you can adjust your sleep habits to ensure your midpoint of sleep falls within the ideal window:\n" +
                                "\n" +
                                "*   **Set a consistent bedtime:** Go to bed at the same time every night to regulate your sleep cycle. If you want your midpoint of sleep to be around 2 a.m., aim to sleep between 10 p.m. and 6 a.m.\n" +
                                "    \n" +
                                "*   **Wake up at the same time each day:** Consistency is key. Waking up at the same time every day helps reinforce your body’s natural sleep patterns.\n" +
                                "    \n" +
                                "*   **Adjust gradually:** If your current sleep schedule is far off from the ideal midpoint, make gradual adjustments. Shift your bedtime and wake-up time by 15-30 minutes each day until you reach the desired timing.\n",
                        img = R.drawable.image_learn_common,
                        internalImg = R.drawable.image_learn_common_int
                    )
                )
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep timing",
                        title = "The benefits of aligning your sleep timing",
                        content = "**Midpoint of sleep is crucial**\n" +
                                "\n" +
                                "Your body’s internal clock, known as the circadian rhythm, is designed to follow the natural cycle of day and night. When your midpoint of sleep falls between 12 a.m. and 3 a.m., you are more in sync with your circadian rhythm. This alignment is crucial for several reasons:\n" +
                                "\n" +
                                "*   **Enhanced sleep quality:** Aligning your sleep with your circadian rhythm allows you to experience deeper, more restorative sleep stages, including deep sleep and REM sleep.\n" +
                                "    \n" +
                                "*   **Improved mental health:** Proper sleep timing can help regulate mood and reduce the risk of mental health issues such as depression and anxiety.\n" +
                                "    \n" +
                                "*   **Better physical health:** When your sleep timing is aligned, you reduce the risk of chronic health conditions like heart disease and metabolic disorders.\n" +
                                "    \n" +
                                "\n" +
                                "**Benefits of having your midpoint of sleep between 12 a.m. and 3 a.m.**\n" +
                                "\n" +
                                "1.  **Optimal hormone regulation:** Your body releases various hormones during sleep, including melatonin, which helps regulate sleep, and cortisol, which helps you wake up. Having your midpoint of sleep in the ideal range ensures that these hormones are released at the right times, supporting overall health.\n" +
                                "    \n" +
                                "2.  **Increased daytime alertness:** When your sleep is aligned with your natural rhythm, you’re more likely to wake up feeling refreshed and alert. This leads to better focus, productivity, and energy levels throughout the day.\n" +
                                "    \n" +
                                "3.  **Lower risk of chronic illness:** Proper sleep timing has been linked to a reduced risk of developing chronic conditions. By maintaining a midpoint of sleep between 12 a.m. and 3 a.m., you help protect your body against issues like obesity, diabetes, and cardiovascular disease.\n" +
                                "    \n" +
                                "4.  **Enhanced cognitive function:** Sleeping during the right time frame supports brain health, helping with memory consolidation and decision-making skills. You’re likely to experience sharper thinking and improved cognitive function when your sleep timing is aligned.\n" +
                                "    \n" +
                                "\n" +
                                "**How to maintain the ideal midpoint of sleep**\n" +
                                "\n" +
                                "*   **Stick to a routine:** Consistency in your sleep schedule is key. Go to bed and wake up at the same time every day, even on weekends.\n" +
                                "    \n" +
                                "*   **Create a sleep-friendly environment:** Make sure your bedroom is conducive to sleep—dark, quiet, and cool.\n" +
                                "    \n" +
                                "*   **Avoid late-night distractions:** Limit exposure to screens and bright lights before bedtime to help your body prepare for sleep.\n",
                        img = R.drawable.image_learn_timing,
                        internalImg = R.drawable.image_learn_timing_int
                    )
                )
            }

            SleepInternalLaunchState.EFFICIENCY -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep efficiency",
                        title = "Understanding sleep efficiency",
                        content = "**Introduction**\n" +
                                "\n" +
                                "Sleep efficiency is a key measure of sleep quality that indicates how effectively you sleep during the time you spend in bed. It is calculated as the ratio of total sleep duration to the total time spent in bed, excluding periods of wakefulness. Ideally, a sleep efficiency rate of 85% or higher is considered optimal for achieving restorative sleep. Understanding sleep efficiency and knowing how to improve it can significantly enhance overall health and well-being.\n" +
                                "\n" +
                                "**What is sleep efficiency?**\n" +
                                "\n" +
                                "Sleep efficiency is a percentage that reflects how much of your time in bed is spent actually sleeping. For instance, if you spend 8 hours in bed but are only asleep for 6.5 of those hours, your sleep efficiency would be 81.25%. This means that 18.75% of your time in bed is spent awake, which might indicate issues such as difficulty falling asleep, staying asleep, or waking up frequently during the night.\n" +
                                "\n" +
                                "Sleep efficiency considers all sleep stages—light sleep, deep sleep, and REM sleep—except for the time you are awake. A higher sleep efficiency percentage indicates that you are getting more restorative sleep, which is crucial for overall health.\n" +
                                "\n" +
                                "**Factors that affect sleep efficiency**\n" +
                                "\n" +
                                "Several factors can influence sleep efficiency:\n" +
                                "\n" +
                                "*   **Sleep environment:** A comfortable, quiet, and dark sleep environment promotes higher sleep efficiency. Minimizing noise and light, and keeping the room at a comfortable temperature, can help.\n" +
                                "    \n" +
                                "*   **Sleep schedule:** Consistency in going to bed and waking up at the same time each day helps regulate your circadian rhythm, which in turn improves sleep efficiency.\n" +
                                "    \n" +
                                "*   **Stress and anxiety:** High stress or anxiety levels can make it difficult to fall asleep or stay asleep, reducing sleep efficiency. Relaxation techniques such as meditation or deep breathing can help manage stress and improve sleep efficiency.\n" +
                                "    \n" +
                                "*   **Diet and substance use:** Consuming caffeine, nicotine, or alcohol, especially close to bedtime, can interfere with sleep and lower sleep efficiency. Avoiding these substances in the evening can help improve sleep efficiency.\n" +
                                "    \n" +
                                "*   **Physical activity:** Regular physical activity can enhance sleep efficiency, though it's best to avoid vigorous exercise right before bed.\n" +
                                "    \n" +
                                "\n" +
                                "**How to improve sleep efficiency**\n" +
                                "\n" +
                                "Improving sleep efficiency involves making adjustments to your sleep habits and environment. Here are some strategies:\n" +
                                "\n" +
                                "*   **Stick to a regular sleep schedule:** Going to bed and waking up at the same time every day helps maintain your body's internal clock, improving sleep efficiency.\n" +
                                "    \n" +
                                "*   **Create a calming pre-sleep routine:** Engaging in relaxing activities before bed, such as reading or taking a warm bath, can help signal to your body that it's time to sleep.\n" +
                                "    \n" +
                                "*   **Optimize your sleep environment:** Ensure that your bedroom is conducive to sleep by keeping it dark, quiet, and cool.\n" +
                                "    \n" +
                                "*   **Limit naps:** While short naps can be beneficial, long or late-afternoon naps may disrupt your nighttime sleep, lowering sleep efficiency.\n" +
                                "    \n" +
                                "\n" +
                                "**Monitor your sleep patterns**\n" +
                                "\n" +
                                "Using a sleep tracker can help you monitor your sleep efficiency and identify patterns that might be affecting your sleep.\n",
                        img = R.drawable.image_learn_common,
                        internalImg = R.drawable.image_learn_common_int
                    )
                )
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep efficiency",
                        title = "The benefits of sleep efficiency",
                        content = "**Introduction**\n" +
                                "\n" +
                                "High sleep efficiency, defined as spending at least 85% of your time in bed actually sleeping, is a key indicator of good sleep quality. Achieving high sleep efficiency is crucial for overall health, as it ensures that your time in bed is being used effectively for rest and recovery. Understanding the benefits of high sleep efficiency can motivate you to prioritize your sleep and make necessary adjustments to your sleep habits.\n" +
                                "\n" +
                                "**Enhanced physical health**\n" +
                                "\n" +
                                "High sleep efficiency contributes significantly to physical health. When sleep efficiency is high, the body is better able to engage in restorative processes, such as tissue repair, muscle growth, and immune system strengthening. During sleep, especially in the deeper stages, the body produces growth hormone, which is essential for recovery and regeneration. High sleep efficiency ensures that you spend sufficient time in these restorative stages, supporting physical health and resilience.\n" +
                                "\n" +
                                "Additionally, high sleep efficiency helps regulate metabolism. Efficient sleep allows the body to properly balance ghrelin and leptin, the hormones responsible for hunger and satiety. This balance reduces the risk of overeating and supports a healthy weight, lowering the risk of obesity and related metabolic conditions.\n" +
                                "\n" +
                                "**Improved cognitive function**\n" +
                                "\n" +
                                "High sleep efficiency also supports cognitive health. During sleep, the brain consolidates memories, processes information, and clears out metabolic waste products. Efficient sleep means that you are spending more time in REM and deep sleep stages, where these critical processes occur. This leads to better memory retention, improved learning, and enhanced problem-solving abilities.\n" +
                                "\n" +
                                "People with high sleep efficiency often experience greater mental clarity and focus during the day, as their brains have had adequate time to rest and recharge. This improved cognitive function can translate into better performance at work or school and a greater ability to handle complex tasks.\n" +
                                "\n" +
                                "**Emotional and mental well-being**\n" +
                                "\n" +
                                "Sleep efficiency is closely tied to emotional regulation and mental health. High sleep efficiency ensures that you are getting enough quality sleep, which is crucial for maintaining emotional stability. During sleep, especially in REM stages, the brain processes emotions and helps to mitigate the effects of stress. Efficient sleep allows for this emotional processing, leading to better mood regulation and reduced anxiety.\n" +
                                "\n" +
                                "Moreover, high sleep efficiency has been associated with a lower risk of developing mood disorders such as depression. By spending more time in restorative sleep, you support the brain's ability to manage stress and maintain a balanced emotional state.\n" +
                                "\n" +
                                "**Better immune function**\n" +
                                "\n" +
                                "A strong immune system is another benefit of high sleep efficiency. Sleep plays a critical role in the functioning of the immune system, including the production of **cytokines**, which are proteins that help fight off infections and inflammation. High sleep efficiency ensures that the body has adequate time to produce and regulate these immune components, reducing the likelihood of illness and improving recovery times.\n" +
                                "\n" +
                                "**Long-term health benefits**\n" +
                                "\n" +
                                "Maintaining high sleep efficiency over the long term can contribute to a lower risk of chronic health conditions, including heart disease, diabetes, and stroke. By ensuring that you get sufficient quality sleep, you support the body's natural repair processes and reduce the wear and tear that can lead to chronic disease.\n",
                        img = R.drawable.image_learn_efficiency,
                        internalImg = R.drawable.image_learn_efficiency_int
                    )
                )
            }

            SleepInternalLaunchState.REM_SLEEP -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "REM sleep",
                        title = "Understanding REM sleep",
                        content = "**What is REM sleep?**\n" +
                                "\n" +
                                " REM (rapid eye movement) sleep is a unique phase of your sleep cycle where most dreaming occurs. It’s characterized by rapid movements of the eyes, increased brain activity, and temporary muscle paralysis. REM sleep is crucial for cognitive functions like learning, memory, and mood regulation.**Sleep stages and dream cycles**\n" +
                                "\n" +
                                " Your sleep cycle alternates between REM and NREM (non-rapid eye movement) stages. REM sleep typically occurs in longer periods during the latter part of the night. It’s during this stage that your brain processes emotions, memories, and new information.\n" +
                                "\n" +
                                " **Optimal ranges**\n" +
                                "\n" +
                                " **For optimal health, aim for REM sleep of 20-25% of your total sleep time**\n" +
                                "\n" +
                                " **How to improve REM sleep?**\n" +
                                "\n" +
                                " Want to boost your REM sleep for better mental and emotional health? Let’s explore some effective strategies to help you increase your REM sleep and wake up feeling more refreshed.\n" +
                                "\n" +
                                " *   **Maintain a consistent sleep schedule**Go to bed and wake up at the same time every day, even on weekends. This helps regulate your sleep cycles.\n" +
                                " \t\n" +
                                " *   **Create a relaxing bedtime routine**Engage in calming activities before bed, such as reading or taking a warm bath.\n" +
                                " \t\n" +
                                " *   **Optimize your sleep environment**Ensure your bedroom is cool, dark, and quiet. Invest in a comfortable mattress and pillows.\n" +
                                " \t\n" +
                                " *   **Limit exposure to screens**Reduce screen time at least an hour before bed. The blue light from screens can interfere with your body’s production of melatonin.\n" +
                                " \t\n" +
                                " *   **Monitor your diet and exercise**Avoid heavy meals, caffeine, and alcohol close to bedtime. Regular physical activity can also promote better sleep.\n" +
                                " \t\n" +
                                "\n" +
                                " **Conclusion**\n" +
                                "\n" +
                                " REM sleep is essential for your cognitive and emotional health. By understanding its importance and making small adjustments to your routine and environment, you can ensure you’re getting enough REM sleep. Sweet dreams!",
                        img = R.drawable.image_learn_common,
                        internalImg = R.drawable.image_learn_common_int
                    )
                )

                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "REM sleep",
                        title = "The benefits of REM sleep",
                        content = "Although it occupies a smaller portion of the sleep cycle compared to other stages, REM sleep plays a vital role in cognitive function, emotional regulation, and overall health. Understanding the benefits of REM sleep can help individuals appreciate its importance and take steps to ensure they achieve sufficient REM sleep each night.\n" +
                                "\n" +
                                " **Cognitive benefits of REM sleep**\n" +
                                "\n" +
                                " REM sleep is closely linked to cognitive processes, particularly those involved in learning and memory. During this stage, the brain actively consolidates and processes information acquired during the day, transferring memories from short-term to long-term storage. This process is crucial for the retention of knowledge and skills, making REM sleep indispensable for effective learning and problem-solving.\n" +
                                "\n" +
                                " Studies have shown that individuals who experience adequate REM sleep perform better on tasks requiring creative thinking and complex decision-making. The brain's ability to make connections between seemingly unrelated pieces of information is enhanced during REM sleep, fostering creativity and innovation.\n" +
                                "\n" +
                                " **Emotional regulation and mental health**\n" +
                                "\n" +
                                " REM sleep also plays a significant role in emotional regulation. During this stage, the brain processes and integrates emotional experiences, helping to stabilize mood and reduce the intensity of negative emotions. The amygdala, a brain region involved in emotional processing, is particularly active during REM sleep, allowing the brain to process emotional memories and reduce their impact on waking life.\n" +
                                "\n" +
                                " Lack of REM sleep has been associated with increased emotional reactivity and a heightened risk of mood disorders such as depression and anxiety. By ensuring adequate REM sleep, individuals can improve their emotional resilience and mental well-being.\n" +
                                "\n" +
                                " **Physical health and REM sleep**\n" +
                                "\n" +
                                " Although REM sleep is primarily associated with cognitive and emotional benefits, it also contributes to physical health. The body's stress hormone levels, such as cortisol, are regulated during REM sleep, helping to reduce overall stress levels. Additionally, REM sleep is thought to play a role in thermoregulation, the process by which the body maintains its internal temperature.\n" +
                                "\n" +
                                " Furthermore, REM sleep supports the body's immune function by modulating the activity of cytokines and other immune cells, which are vital for fighting off infections and inflammation. Insufficient REM sleep can weaken the immune response, making individuals more susceptible to illnesses.\n" +
                                "\n" +
                                " **Dreaming and psychological insight**\n" +
                                "\n" +
                                " One of the most well-known aspects of REM sleep is dreaming. While dreams can occur in other sleep stages, they are most vivid and complex during REM sleep. Dreaming allows the brain to explore thoughts and emotions in a symbolic and often abstract manner, providing psychological insights that may not be accessible during waking hours.\n" +
                                "\n" +
                                " Some researchers believe that dreaming during REM sleep serves as a form of cognitive rehearsal, where the brain practices responses to potential future scenarios, thereby enhancing problem-solving skills and adaptability.",
                        img = R.drawable.image_learn_rem,
                        internalImg = R.drawable.image_learn_rem_int
                    )
                )
            }

            SleepInternalLaunchState.DEEP_SLEEP -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Deep sleep",
                        title = "Understanding deep sleep",
                        content = "**What is deep sleep?**\n" +
                                "\n" +
                                "Deep sleep, also known as delta waves typically occurs during the first half of the night and is the most restorative part of the sleep cycle. During deep sleep, the body focuses on repairing tissues, building muscle, and strengthening the immune system. The brain also processes and consolidates memories, making deep sleep essential for both physical and mental health.\n" +
                                "\n" +
                                "**Optimal ranges**\n" +
                                "\n" +
                                "For optimal health, aim for deep sleep: 13-23% of your total sleep time.\n" +
                                "\n" +
                                "**Characteristics of deep sleep**\n" +
                                "\n" +
                                "During deep sleep, the body experiences a drop in heart rate, breathing rate, and body temperature. Muscles relax completely, and the body releases growth hormone, which is critical for tissue repair and growth. This stage of sleep is also associated with minimal brain activity, allowing the brain to recover from the demands of the day.\n" +
                                "\n" +
                                "**Why is deep sleep important?**\n" +
                                "\n" +
                                "Deep sleep is crucial for the body's overall recovery and rejuvenation. It supports various bodily functions, including immune system efficiency, hormone balance, and cognitive processing. Without sufficient deep sleep, individuals may experience impaired memory, reduced physical performance, and a weakened immune response.\n" +
                                "\n" +
                                "**How Can deep sleep be improved?**\n" +
                                "\n" +
                                "Improving deep sleep requires a combination of lifestyle changes and sleep hygiene practices. Here are some strategies to enhance deep sleep:\n" +
                                "\n" +
                                "*   **Maintain a consistent sleep schedule:** Going to bed and waking up at the same time every day helps regulate the circadian rhythm, which in turn supports better deep sleep.\n" +
                                "    \n" +
                                "*   **Create a sleep-conducive environment:** Ensure that your bedroom is dark, quiet, and cool, as these conditions promote deep sleep. Consider using blackout curtains, earplugs, or white noise machines to minimize disturbances.\n" +
                                "    \n" +
                                "*   **Limit exposure to screens before bedtime:** The blue light emitted by phones, tablets, and computers can interfere with the production of **melatonin**, the hormone that regulates sleep. Reducing screen time before bed can help improve sleep quality and increase deep sleep.\n" +
                                "    \n" +
                                "*   **Engage in regular physical activity:** Exercise has been shown to enhance deep sleep, but it's important to avoid vigorous activity close to bedtime, as it can have the opposite effect.\n" +
                                "    \n" +
                                "*   **Avoid caffeine and heavy meals before bedtime:** Caffeine and large meals can disrupt sleep patterns and reduce the amount of deep sleep. Aim to have your last meal a few hours before bedtime and limit caffeine intake in the afternoon and evening.\n" +
                                "    \n" +
                                "*   **Practice relaxation techniques:** Activities such as meditation, deep breathing, or a warm bath before bed can help calm the mind and body, making it easier to enter deep sleep.\n",
                        img = R.drawable.image_learn_common,
                        internalImg = R.drawable.image_learn_common_int
                    )
                )
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Deep sleep",
                        title = "The benefits of Deep sleep",
                        content = "**Introduction**\n" +
                                "\n" +
                                "Deep sleep is often referred to as the \"restorative\" stage of sleep, and for good reason. This stage of the sleep cycle is essential for physical recovery, mental clarity, and overall health. The benefits of deep sleep extend far beyond simply feeling rested, impacting nearly every aspect of well-being.\n" +
                                "\n" +
                                "**Physical restoration**\n" +
                                "\n" +
                                "One of the most significant benefits of deep sleep is its role in physical restoration. During deep sleep, the body repairs and regenerates tissues, builds muscle and bone, and strengthens the immune system. The release of growth hormone during this stage is critical for these processes, making deep sleep particularly important for athletes and those recovering from injury.\n" +
                                "\n" +
                                "Deep sleep also helps regulate the body's metabolism. During this stage, energy expenditure is reduced, allowing the body to conserve and restore energy. This process is vital for maintaining a healthy metabolism and preventing conditions such as obesity and type 2 diabetes.\n" +
                                "\n" +
                                "**Cognitive benefits**\n" +
                                "\n" +
                                "Deep sleep is closely linked to cognitive function, particularly in the areas of memory consolidation and learning. During deep sleep, the brain processes and stores information acquired during the day, transferring memories from short-term to long-term storage. This process is essential for learning new skills and retaining knowledge.\n" +
                                "\n" +
                                "In addition to memory consolidation, deep sleep supports problem-solving and decision-making abilities. Research suggests that individuals who experience sufficient deep sleep perform better on tasks requiring complex thinking and creativity.\n" +
                                "\n" +
                                "**Emotional and mental health**\n" +
                                "\n" +
                                "Deep sleep also plays a crucial role in emotional regulation and mental health. During this stage, the brain processes and integrates emotional experiences, helping to stabilize mood and reduce the intensity of negative emotions. Lack of deep sleep has been associated with increased stress, anxiety, and a heightened risk of mood disorders such as depression.\n" +
                                "\n" +
                                "Deep sleep's impact on the brain's prefrontal cortex—the area responsible for decision-making, social interactions, and emotional responses—further underscores its importance for mental health. Ensuring adequate deep sleep can help individuals better manage stress, improve emotional resilience, and maintain mental well-being.\n" +
                                "\n" +
                                "**Immune function and disease prevention**\n" +
                                "\n" +
                                "Deep sleep is essential for a healthy immune system. During this stage, the body produces and releases cytokines, proteins that help fight off infections and inflammation. Adequate deep sleep strengthens the immune response, reducing the risk of illnesses such as the common cold, flu, and other infections.\n" +
                                "\n" +
                                "Moreover, deep sleep has been linked to a lower risk of chronic diseases, including heart disease, diabetes, and stroke. By supporting the body's repair processes and immune function, deep sleep helps prevent the onset of these conditions and promotes long-term health.\n",
                        img = R.drawable.image_learn_deep,
                        internalImg = R.drawable.image_learn_deep_int
                    )
                )
            }

            SleepInternalLaunchState.SLEEP_DURATION -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "**Understanding sleep duration**\n" +
                                "\n" +
                                " **Introduction**\n\nSleep duration is a critical component of overall health and well-being. It refers to the total amount of sleep an individual gets each night and plays a crucial role in various bodily functions, including cognitive performance, physical recovery, and emotional regulation. Understanding the concept of sleep duration and how it varies across different stages of life is essential for optimizing health.\n" +
                                "\n" +
                                " **The basics of sleep duration**\n\nSleep duration is the amount of time spent asleep from the moment you fall asleep until you wake up. This period encompasses all stages of sleep, including light sleep, deep sleep, and REM (rapid eye movement) sleep, each of which is vital for different aspects of physical and mental recovery.\n" +
                                "\n" +
                                " **Why sleep duration matters**\n\nAdequate sleep duration is necessary for the body to repair tissues, regulate hormones, and consolidate memories. Chronic sleep deprivation, where an individual consistently gets less sleep than needed, can lead to various health issues such as weakened immune function, increased risk of chronic diseases, impaired cognitive function, and mood disturbances.\n" +
                                "\n" +
                                " **Sleep duration across different age groups** Sleep needs are not static and change throughout the human lifespan.\n" +
                                "\n" +
                                " *   **Infants and young children:** In the early stages of life, sleep is essential for growth and development. Infants typically require 14 to 17 hours of sleep, which gradually decreases as they grow older. By the time children reach school age, they generally need 9 to 11 hours of sleep.\n" +
                                " \t\n" +
                                " *   **Teenagers:** Adolescents need about 8 to 10 hours of sleep to support their rapid physical and cognitive development. This stage often sees a shift in sleep patterns, with teenagers naturally inclined to stay up later, which can conflict with early school start times.\n" +
                                " \t\n" +
                                " *   **Adults:** **Most adults function best with 7 to 9 hours of sleep.** This range helps maintain optimal cognitive performance, emotional stability, and physical health.\n" +
                                " \t\n" +
                                " *   **Older adults:** Although the amount of sleep needed doesn’t drastically change in older age, sleep patterns can shift. Older adults may find it more challenging to achieve continuous, deep sleep, but it remains important to aim for 7 to 8 hours to support overall health.\n" +
                                " \t\n" +
                                "\n" +
                                " **Factors affecting sleep duration**\n\nSeveral factors can influence how much sleep you get, including lifestyle choices, sleep environment, and health conditions. Stress, caffeine, screen time, and irregular sleep schedules can all negatively impact sleep duration. Understanding these factors and how they affect your sleep can help you make adjustments to improve your sleep quality and duration.",
                        img = R.drawable.image_learn_common,
                        internalImg = R.drawable.image_learn_common_int
                    )
                )
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "The importance of sleep duration for health and wellness",
                        content = "**Introduction**\n" +
                                "\n" +
                                " Sleep is a fundamental pillar of health, affecting nearly every system in the body. Adequate sleep duration is essential not only for feeling rested but also for supporting a wide range of physiological and cognitive functions. Understanding the importance of sleep duration, along with the science behind it, can help individuals prioritize their sleep and, in turn, their overall health.\n" +
                                "\n" +
                                " **Impact on physical health**\n" +
                                "\n" +
                                " Sleep duration has a profound impact on physical health. During sleep, the body undergoes various restorative processes that are critical for maintaining homeostasis. For example, the regulation of endocrine function—specifically the release of hormones such as growth hormone (GH) and cortisol—occurs during sleep. Growth hormone is particularly important during deep sleep (slow-wave sleep), where it stimulates tissue growth and muscle repair.\n" +
                                "\n" +
                                " Moreover, sleep is crucial for immune function. Research shows that adequate sleep enhances the activity of cytokines and other immune cells, which play a key role in defending the body against infections and diseases. Conversely, chronic sleep deprivation is associated with an increased risk of cardiovascular diseases, such as hypertension, due to its impact on autonomic nervous system (ANS) regulation, leading to heightened sympathetic activity and reduced parasympathetic tone.\n" +
                                "\n" +
                                " **Cognitive and mental health**\n" +
                                "\n" +
                                " Sleep duration is also intimately linked with cognitive function and mental health. The hippocampus, a region of the brain involved in memory formation, is particularly active during REM sleep, where the consolidation of memories occurs. Sufficient sleep ensures that information and experiences are processed and stored efficiently, which is essential for learning and memory retention.\n" +
                                "\n" +
                                " In terms of mental health, sleep duration influences the balance of neurotransmitters, such as serotonin and dopamine, which regulate mood and emotional stability. Sleep deprivation disrupts this balance, often leading to symptoms of anxiety, depression, and mood swings. Furthermore, prolonged inadequate sleep can increase the risk of developing neurodegenerative conditions like Alzheimer's disease, as the brain's clearance of beta-amyloid plaques—proteins associated with cognitive decline—is hindered by lack of sleep.\n" +
                                "\n" +
                                " **Sleep duration and longevity**\n" +
                                "\n" +
                                " Emerging research suggests that sleep duration may be linked to longevity. Studies indicate that both short and excessively long sleep durations are associated with increased mortality risk. This U-shaped curve highlights the importance of maintaining an optimal sleep duration, generally between 7 to 9 hours for adults, to promote longevity and reduce the risk of chronic conditions such as obesity, diabetes, and cardiovascular diseases.\n" +
                                "\n" +
                                " Sleep also plays a role in metabolic regulation. Insufficient sleep disrupts the balance of ghrelin and leptin, hormones that regulate appetite. This disruption can lead to increased hunger, overeating, and eventually weight gain, which are risk factors for metabolic disorders like type 2 diabetes.\n" +
                                "\n" +
                                " **Guidelines for optimizing sleep duration**\n" +
                                "\n" +
                                " To optimize sleep duration and enhance overall health, consider the following guidelines:\n" +
                                "\n" +
                                " *   **Maintain a consistent sleep schedule:** Going to bed and waking up at the same time every day helps regulate the circadian rhythm, the body's internal clock that dictates sleep-wake cycles.\n" +
                                " \t\n" +
                                " *   **Create a sleep-conducive environment:** Ensure that your bedroom is dark, quiet, and cool to promote the production of **melatonin**, the hormone that regulates sleep.\n" +
                                " \t\n" +
                                " *   **Limit exposure to blue light:** Blue light from screens can interfere with melatonin production. Limiting screen time before bed can help improve sleep quality and duration.\n" +
                                " \t\n" +
                                " *   **Manage stress effectively:**Chronic stress increases levels of **cortisol**, a hormone that can disrupt sleep if elevated at night. Techniques such as mindfulness, deep breathing, and regular physical activity can help manage stress",
                        img = R.drawable.image_learn_duration,
                        internalImg = R.drawable.image_learn_duration_int
                    )
                )
            }

            SleepInternalLaunchState.LATENCY -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep latency",
                        title = "Understanding sleep latency",
                        content = "**What is sleep latency?**\n" +
                                "\n" +
                                "Sleep latency is essentially the time it takes to fall asleep after lying down. This period is when the body and mind gradually relax, preparing for the onset of sleep. During this time, the brain slows down, and the body begins to cool down, signaling that it’s time to sleep.\n" +
                                "\n" +
                                "**The ideal sleep latency is between 5 to 20 minutes.** Falling asleep within this time frame typically indicates that the body is adequately prepared for rest. However, deviations from this range can signal potential sleep-related problems or other health issues.\n" +
                                "\n" +
                                "**Why sleep latency matters**?\n" +
                                "\n" +
                                "Sleep latency is an important indicator of sleep quality and overall health. If a person consistently falls asleep in less than 5 minutes, it may indicate that they are overly tired or sleep-deprived. This could be due to factors such as insufficient sleep, excessive physical activity, or high levels of stress and fatigue.\n" +
                                "\n" +
                                "On the other hand, if it takes more than 20 minutes to fall asleep, this might suggest difficulty in winding down, possibly due to anxiety, stress, or poor sleep hygiene. Persistent long sleep latency can lead to frustration and anxiety about not being able to fall asleep, which can further exacerbate sleep problems.\n" +
                                "\n" +
                                "**Factors affecting sleep latency**\n" +
                                "\n" +
                                "Several factors can influence how long it takes to fall asleep, including:\n" +
                                "\n" +
                                "*   **Sleep environment:** A noisy, bright, or uncomfortable sleep environment can make it harder to fall asleep quickly. Optimizing your sleep environment by keeping it dark, quiet, and cool can help reduce sleep latency.\n" +
                                "    \n" +
                                "*   **Stress and anxiety:** High levels of stress or anxiety can keep the mind active, making it difficult to relax and fall asleep. Incorporating relaxation techniques, such as deep breathing, meditation, or progressive muscle relaxation, can help reduce sleep latency.\n" +
                                "    \n" +
                                "*   **Sleep habits:** Irregular sleep patterns or poor sleep hygiene, such as inconsistent bedtimes, exposure to screens before bed, or consumption of caffeine and heavy meals in the evening, can all extend sleep latency.\n" +
                                "    \n" +
                                "*   **Physical health:** Conditions such as sleep apnea, restless leg syndrome, or chronic pain can also increase sleep latency by making it difficult to get comfortable and fall asleep.\n" +
                                "    \n" +
                                "\n" +
                                "**How to achieve ideal sleep latency** ?\n" +
                                "\n" +
                                "Achieving the ideal sleep latency of 5 to 20 minutes involves making adjustments to your lifestyle and sleep habits. Here are some strategies:\n" +
                                "\n" +
                                "*   **Establish a consistent sleep routine:** Going to bed and waking up at the same time every day helps regulate your body’s internal clock, making it easier to fall asleep within the ideal time frame.\n" +
                                "    \n" +
                                "*   **Create a relaxing pre-sleep ritual:** Engage in calming activities before bed, such as reading, listening to soothing music, or taking a warm bath. This can signal to your body that it’s time to sleep and help reduce sleep latency.\n" +
                                "    \n" +
                                "*   **Optimize your sleep environment:** Ensure that your bedroom is conducive to sleep by keeping it dark, quiet, and cool.\n" +
                                "    \n" +
                                "*   **Limit screen time before bed:** The blue light emitted by screens can interfere with the production of melatonin, the hormone that regulates sleep. Limiting screen time in the hour before bed can help improve sleep latency.\n",
                        img = R.drawable.image_learn_common,
                        internalImg = R.drawable.image_learn_common_int
                    )
                )
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep latency",
                        title = "Why optimal sleep latency matters?",
                        content = "Maintaining an optimal sleep latency is crucial for ensuring that your body and mind receive the restorative rest they need. Falling asleep too quickly—within less than 5 minutes—often indicates extreme tiredness or sleep deprivation. This can be a sign that your body is not getting enough sleep on a regular basis, which can have negative effects on your health, including impaired cognitive function, weakened immune system, and increased stress levels.\n" +
                                "\n" +
                                "Conversely, taking more than 20 minutes to fall asleep can be a sign of sleep difficulties, such as insomnia or anxiety. This can lead to frustration and anxiety about not being able to fall asleep, which can create a cycle of poor sleep and prolonged sleep latency. By achieving and maintaining an optimal sleep latency, you can improve your overall sleep quality and prevent these issues.\n" +
                                "\n" +
                                "**Benefits of achieving optimal sleep latency**\n" +
                                "\n" +
                                "1.  **Improved sleep quality :** When you fall asleep within the ideal 5 to 20 minutes, it suggests that your body is ready for sleep, and you are more likely to experience a full and restorative sleep cycle. This includes sufficient time in deep sleep and REM sleep, both of which are essential for physical recovery, memory consolidation, and emotional regulation.\n" +
                                "    \n" +
                                "2.  **Enhanced cognitive function:** Optimal sleep latency is associated with better cognitive performance. When you fall asleep quickly and efficiently, your brain has more time to undergo processes that consolidate memories, process information, and clear out metabolic waste products. This can lead to improved focus, better decision-making, and enhanced problem-solving abilities during the day.\n" +
                                "    \n" +
                                "3.  **Better emotional and mental health:** Maintaining an optimal sleep latency helps regulate your mood and reduce the risk of mental health issues such as anxiety and depression. When you consistently fall asleep within the ideal time frame, you are less likely to experience the frustration and anxiety associated with difficulty falling asleep. This can contribute to a more stable mood and better overall mental health.\n" +
                                "    \n" +
                                "4.  **Reduced stress levels:** Falling asleep within the optimal sleep latency range helps reduce stress levels by allowing your body to enter a state of relaxation more quickly. This reduces the time spent in wakefulness, where stress and worries can dominate your thoughts, and helps you achieve a more restful sleep.\n" +
                                "    \n" +
                                "5.  **Long-term health benefits:** Consistently achieving optimal sleep latency can contribute to better long-term health outcomes. By ensuring that you are falling asleep within the ideal time frame, you reduce the risk of developing chronic sleep issues, which can lead to other health problems such as cardiovascular disease, obesity, and weakened immune function. Maintaining healthy sleep latency supports overall health and longevity.\n",
                        img = R.drawable.image_learn_latency,
                        internalImg = R.drawable.image_learn_latency_int
                    )
                )
            }

            SleepInternalLaunchState.RESTFULNESS -> {
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Restfulness",
                        title = "Understanding restfulness",
                        content = "**Introduction**\n" +
                                "\n" +
                                "Restfulness during sleep is about how peaceful your sleep is. The Luna ring tracks your movements while you sleep and sorts them into low, medium, and high. High movements are the ones that disrupt your sleep the most. To get good rest, it’s best to have less than 3 high movements during the night.\n" +
                                "\n" +
                                "**Why high movements disrupt your sleep?**\n" +
                                "\n" +
                                "High movements mean your sleep is being disturbed. When you have too many high movements, it can stop you from getting deep, restful sleep. This can leave you feeling tired and not refreshed, even if you sleep for a long time.\n" +
                                "\n" +
                                "**What causes high movements?**\n" +
                                "\n" +
                                "Several things can lead to more high movements during sleep:\n" +
                                "\n" +
                                "*   **Sleep environment:** Noise, light, or an uncomfortable bed can make you move more during sleep.\n" +
                                "    \n" +
                                "*   **Stress and anxiety:** If you’re stressed, your body might be restless, causing more high movements.\n" +
                                "    \n" +
                                "*   **Diet and lifestyle:** Eating heavy meals or drinking caffeine close to bedtime can increase restlessness.\n" +
                                "    \n" +
                                "\n" +
                                "**How to reduce high movements** ?\n" +
                                "\n" +
                                "Here’s how you can lower the number of high movements and sleep better:\n" +
                                "\n" +
                                "*   **Create a calm sleep space:** Make sure your bedroom is quiet, dark, and comfortable.\n" +
                                "    \n" +
                                "*   **Relax before bed:** Do something calming, like deep breathing or reading, to help your body relax.\n" +
                                "    \n" +
                                "*   **Keep a regular sleep schedule:** Go to bed and wake up at the same time every day to help your body sleep better.\n",
                        img = R.drawable.image_learn_common,
                        internalImg = R.drawable.image_learn_common_int
                    )
                )
                dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Restfulness",
                        title = "The benefits of minimizing high movements for better restfulness",
                        content = "High movements during sleep indicate moments when your body is restless, often causing you to wake up or move between lighter stages of sleep. These disruptions prevent you from spending enough time in deep sleep and REM sleep, which are essential for physical recovery and mental rejuvenation.\n" +
                                "\n" +
                                "**The Benefits of reducing high movements**\n" +
                                "\n" +
                                "1.  **Enhanced physical recovery:** When high movements are minimized, your body can stay in deep sleep longer, allowing for more effective physical repair and growth. This is particularly important for muscle recovery and overall health.\n" +
                                "    \n" +
                                "2.  **Improved mental clarity:** Reducing high movements helps ensure that your brain spends adequate time in REM sleep, which is crucial for memory consolidation and cognitive function. Fewer high movements mean better mental clarity and sharper focus during the day.\n" +
                                "    \n" +
                                "3.  **Balanced emotional health:** Sleep that is free from frequent high movements supports emotional regulation. When your sleep is restful, your brain can process emotions more effectively, leading to improved mood and reduced anxiety.\n" +
                                "    \n" +
                                "4.  **Lower stress levels:** High movements during sleep are often linked to stress. By reducing these movements, you help your body stay in a relaxed state throughout the night, which can lower overall stress levels and contribute to a more peaceful waking experience.\n" +
                                "    \n" +
                                "5.  **Long-term health benefits:** Consistently achieving sleep with fewer than three high movements can reduce the risk of chronic conditions like heart disease, obesity, and diabetes. Better restfulness leads to better overall health outcomes.\n",
                        img = R.drawable.image_learn_restfulness,
                        internalImg = R.drawable.image_learn_restfulness_int
                    )
                )
            }

            SleepInternalLaunchState.RESPIRATORY_RATE -> {

            }

            SleepInternalLaunchState.RESTING_HEART_RATE -> {

            }

            SleepInternalLaunchState.HRV -> {

            }

            SleepInternalLaunchState.SKIN_TEMPERATURE -> {

            }

            SleepInternalLaunchState.BLOOD_OXYGEN -> {
                /*dataList.add(
                    SleepLearnMoreDataModel(
                        toolbarTitle = "Sleep duration",
                        title = "Understanding sleep duration",
                        content = "2 min read",
                        img = R.drawable.img_hr_article_1,
                        internalImg = R.drawable.img_hr_article_1
                    )
                )*/
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
            SleepInternalLaunchState.SLEEP_DURATION, SleepInternalLaunchState.SLEEP_TIME,
            SleepInternalLaunchState.REM_SLEEP, SleepInternalLaunchState.DEEP_SLEEP -> {
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
    val time: String? = null,
    val trendsData: TrendAverage? = null,
    val dailyValue: Float? = null,
)

enum class InternalSelectedPeriod {
    DAILY, DAY, WEEK, MONTH
}

enum class TrendsTopState {
    SINGLE, SINGLE_DATE, DOUBLE_DATE
}