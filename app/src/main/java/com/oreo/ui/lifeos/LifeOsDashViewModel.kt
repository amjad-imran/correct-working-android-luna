package com.oreo.ui.lifeos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.GraphType
import com.oreo.data.dataConverter.GraphDataConvertor
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import com.oreo.data.model.lifeos.dashModels.LifeOsWhatsNewResponse
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.lifeos.charts.InsightCardUiModel
import com.oreo.ui.sleep2.internal.InternalSelectedPeriod
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class LifeOsDashViewModel @Inject constructor(
    val localDataStore: DataStoredInterface,
    val graphDataConvertor: GraphDataConvertor,
    val oreoDeviceRepository: OreoDeviceRepository,
    private val userRepository: UserRepository,
    private val resourcesProvider: ResourcesProvider,
) : BaseViewModel() {

    private val _questions = MutableLiveData<List<String>>()
    val questions: LiveData<List<String>> get() = _questions

    private val _whatsNew = MutableLiveData<LifeOsWhatsNewResponse>()
    val whatsNew: LiveData<LifeOsWhatsNewResponse> get() = _whatsNew

    val destinationData = MutableLiveData<LifeOsDestinations?>()

    private val _insightsCardsData = MutableLiveData<List<InsightCardUiModel>>()
    val insightsCardsData: LiveData<List<InsightCardUiModel>> get() = _insightsCardsData

    fun getLifeOsData(){
        viewModelScope.launch {
            val onBoardData = localDataStore.getLifeOsOnboardData()

            if(onBoardData==null){
                destinationData.postValue(LifeOsDestinations.BEGIN_FRAG)
                return@launch
            }

            val isAttempted = onBoardData.questions?.any { it.isSavedByUser } ?: false
            if(!isAttempted){
                destinationData.postValue(LifeOsDestinations.QUES_FRAG)
                return@launch
            }

            destinationData.postValue(LifeOsDestinations.LIFE_OS_MAIN)
            loadSuggestedQuestions()
            loadWhatsNew()
            loadInsightsData()
            /*val curProgress = 10
            val totalQues = 10
            var destination = LifeOsDestinations.LIFE_OS_MAIN
            if(curProgress==0){
                destination = LifeOsDestinations.BEGIN_FRAG
            }else if(curProgress in 1..totalQues-1){
                destination = LifeOsDestinations.QUES_FRAG
            }

            destinationData.postValue(destination)*/
        }
    }

    /*fun loadSuggestedQuestions() {
        _questions.value = listOf(
            "Teach me about my sleep score",
            "Create a diet plan for me",
            "Create a workout plan for me"
        )
    }*/

    fun loadSuggestedQuestions(aiTopic: AITopics = AITopics.GENERAL) {
        viewModelScope.launch {
            oreoDeviceRepository.getAiTopQuestions(aiTopic).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        /*sendMessage(resource.message)*/
                    }

                    is Resource.Loading -> {
                        /*setLoading(resource.loading)*/
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        loadSuggestedQuestions(aiTopic)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _questions.value = it.questions?.map { it.question as String }
                            //showHistoryIcon.value = it.hasHistory
                        }
                    }
                }
            }
        }


    }

    fun loadInsightsData() {

        /*val jsonRes = """
            [
  [
    {
      "relevancy": 0.95,
      "title": "Total Sleep Time Increased by 2% This Week",
      "description": "Your average nightly total sleep duration improved from 7h 0m to 7h 11m. Even these small gains can make a real difference for your energy, mood, and recovery throughout the day.",
      "suggestions": "Aim to protect this sleep window by keeping your bedtime routine consistent.",
      "related_suggested_questions": [
        "Did you change your bedtime or wake-up habits this week?",
        "Did you notice changes in how refreshed you felt in the mornings?",
        "Were there any late caffeine or screen use evenings that affected your sleep?"
      ],
      "graph_type": "total_duration_day",
      "graph": [
        {
          "date": "2025-11-27",
          "value1": 26370
        },
        {
          "date": "2025-11-28",
          "value1": 20640
        },
        {
          "date": "2025-11-29",
          "value1": null
        },
        {
          "date": "2025-11-30",
          "value1": null
        },
        {
          "date": "2025-12-01",
          "value1": 28020
        },
        {
          "date": "2025-12-02",
          "value1": 32400
        },
        {
          "date": "2025-12-03",
          "value1": 28230
        },
        {
          "date": "2025-12-04",
          "value1": 19350
        }
      ]
    },
    {
      "relevancy": 0.92,
      "title": "More Deep Sleep: Averaging 1h 30m Per Night",
      "description": "You’re averaging 1h 30m of deep sleep each night. This stage is crucial for body repair, immunity, and next-day resilience. Great job supporting your recovery!",
      "suggestions": "Keep alcohol and heavy meals away from late evenings to continue maximizing deep sleep.",
      "related_suggested_questions": [
        "Have you adjusted your wind-down activities in the evenings?",
        "Does your deep sleep correlate with your exercise or alcohol intake?",
        "How do you feel after nights with more or less deep sleep?"
      ],
      "graph_type": "deep_sleep_day",
      "graph": [
        {
          "date": "2025-11-27",
          "value1": 6360
        },
        {
          "date": "2025-11-28",
          "value1": 5070
        },
        {
          "date": "2025-11-29",
          "value1": null
        },
        {
          "date": "2025-11-30",
          "value1": null
        },
        {
          "date": "2025-12-01",
          "value1": 5760
        },
        {
          "date": "2025-12-02",
          "value1": null
        },
        {
          "date": "2025-12-03",
          "value1": 4830
        },
        {
          "date": "2025-12-04",
          "value1": 5100
        }
      ]
    },
    {
      "relevancy": 0.91,
      "title": "Circadian Rhythm Shifted Earlier This Week",
      "description": "Your sleep midpoint has been trending earlier, with your latest midpoint at 3:30 AM versus 4:24 AM one week ago. Shifting your circadian rhythm earlier can help with feeling more refreshed in the mornings.",
      "suggestions": "Try to get morning sunlight exposure to reinforce this earlier rhythm.",
      "related_suggested_questions": [
        "Did you make changes to your evening or morning light exposure?",
        "How does this earlier pattern affect your alertness during the day?",
        "Do late nights or changing routines lead to later mid-sleep points?"
      ],
      "graph_type": "circadian_mid_point_day",
      "graph": [
        {
          "date": "2025-11-27",
          "value1": 17460
        },
        {
          "date": "2025-11-28",
          "value1": 18000
        },
        {
          "date": "2025-11-29",
          "value1": null
        },
        {
          "date": "2025-11-30",
          "value1": null
        },
        {
          "date": "2025-12-01",
          "value1": 17120
        },
        {
          "date": "2025-12-02",
          "value1": 16500
        },
        {
          "date": "2025-12-03",
          "value1": 17977
        },
        {
          "date": "2025-12-04",
          "value1": 15450
        }
      ]
    }
  ],
  [
    {
      "relevancy": 0.96,
      "title": "Total Sleep Duration Decreased 14% This Week Compared to Last Week",
      "description": "You averaged 6h 44m of sleep per night this week, down from 7h 49m last week. This drop may have left you feeling a little less rested and could impact your focus and recovery. Fluctuations like this are normal now and then, but restoring a consistent sleep schedule can quickly help you feel your best again.",
      "suggestions": "Try setting a relaxing wind-down routine and stick to a regular bedtime, even on weekends, to maximize sleep time.",
      "related_suggested_questions": [
        "Did you notice a change in energy, mood, or focus during days with less sleep?",
        "Did anything disrupt your bedtime routine this week?",
        "How does your sleep duration trend relate to your stress or recovery scores?"
      ],
      "graph_type": "total_duration_week",
      "graph": [
        {
          "date": "2025-11-24",
          "value1": 24150
        },
        {
          "date": "2025-11-17",
          "value1": 30525
        },
        {
          "date": "2025-11-10",
          "value1": 27334
        },
        {
          "date": "2025-11-03",
          "value1": 24471
        },
        {
          "date": "2025-10-27",
          "value1": 25800
        },
        {
          "date": "2025-10-20",
          "value1": 28945
        }
      ]
    },
    {
      "relevancy": 0.9,
      "title": "Deep Sleep Increased 10% This Week Compared to Last Week",
      "description": "You spent more time in deep sleep stages this week (average: 1h 24m), up from 1h 17m last week. Deep sleep is when your body repairs and restores itself, which can lead to better muscle recovery, mood, and immune strength. Nice work!",
      "suggestions": "Maintain your deep sleep gains by keeping your bedroom cool, dark, and quiet, and avoiding screens at least 30 minutes before bed.",
      "related_suggested_questions": [
        "Did your daytime activity or stress levels shift on days with more deep sleep?",
        "Have you noticed a difference in how you feel after nights with higher deep sleep?",
        "Has your supplement, caffeine, or alcohol intake changed recently?"
      ],
      "graph_type": "deep_sleep_week",
      "graph": [
        {
          "date": "2025-11-24",
          "value1": 5280
        },
        {
          "date": "2025-11-17",
          "value1": 5568
        },
        {
          "date": "2025-11-10",
          "value1": 4398
        },
        {
          "date": "2025-11-03",
          "value1": 3885
        },
        {
          "date": "2025-10-27",
          "value1": 6098
        },
        {
          "date": "2025-10-20",
          "value1": 6070
        }
      ]
    },
    {
      "relevancy": 0.92,
      "title": "HRV Rose 24% Compared to Your Previous 30-Day Average",
      "description": "Your average HRV this week was 36ms, up from your previous 30-day average of 29ms. Higher HRV signals better resilience and recovery from stress—your body is adapting well, possibly thanks to better sleep or balanced activity.",
      "suggestions": "Support your HRV gains by scheduling regular recovery days and focusing on mindful habits.",
      "related_suggested_questions": [
        "Are you feeling more balanced or energized throughout the week?",
        "Did your bedtime routines, recovery practices, or stress management habits change?",
        "How does HRV relate to your sleep quality or readiness scores this week?"
      ],
      "graph_type": "hrv_week",
      "graph": [
        {
          "date": "2025-11-24",
          "value1": 36
        },
        {
          "date": "2025-11-17",
          "value1": 30
        },
        {
          "date": "2025-11-10",
          "value1": 31
        },
        {
          "date": "2025-11-03",
          "value1": 30
        },
        {
          "date": "2025-10-27",
          "value1": 43
        },
        {
          "date": "2025-10-20",
          "value1": 57
        }
      ]
    }
  ],
  [
    {
      "relevancy": 0.95,
      "title": "Deep Sleep Decreased by 13% This Month Compared to Last Month",
      "description": "Your average deep sleep time dropped from 1h 26m last month to 1h 15m this month, a 13% decrease. Deep sleep is vital for physical restoration and feeling fully refreshed. This dip often happens with inconsistent sleep timing or when stress or high activity isn't balanced with enough recovery. It's worth noting a few late bedtimes and periods of higher sleep debt during the month, which may have contributed.",
      "suggestions": "Aim to anchor your bedtime and wake time to a regular window—even on weekends—to help your body access more restorative deep sleep.",
      "related_suggested_questions": [
        "Did your stress or workload increase during weeks with less deep sleep?",
        "Were there more days of late or disrupted sleep than last month?",
        "Have you noticed any changes in caffeine or alcohol intake before bed?"
      ],
      "graph_type": "deep_sleep_month",
      "graph": [
        {
          "date": "2025-11-01",
          "value1": 4691
        },
        {
          "date": "2025-10-01",
          "value1": 5367
        },
        {
          "date": "2025-09-01",
          "value1": 4902
        },
        {
          "date": "2025-08-01",
          "value1": 4951
        },
        {
          "date": "2025-07-01",
          "value1": 5190
        },
        {
          "date": "2025-06-01",
          "value1": null
        }
      ]
    },
    {
      "relevancy": 0.92,
      "title": "HRV Dropped Significantly This Month",
      "description": "Your average nightly heart rate variability (HRV) fell from 41 ms last month to 30 ms this month—a notable decrease. Lower HRV can signal that your body is dealing with ongoing stress or not getting enough high-quality recovery. This may line up with more sleep debt, higher resting heart rates, or possible lifestyle stressors. Increasing stress scores seen this month also suggest your system was under more pressure.",
      "suggestions": "Prioritize at least one night each week for extra rest, and add a wind-down routine (like stretching or breathwork) before bed to help your body shift out of stress mode.",
      "related_suggested_questions": [
        "Did you have more days of mental or emotional stress this month?",
        "Did nights with better sleep or more deep sleep lead to higher HRV the next day?",
        "Has your training, caffeine, or screen time increased recently?"
      ],
      "graph_type": "hrv_month",
      "graph": [
        {
          "date": "2025-11-01",
          "value1": 31
        },
        {
          "date": "2025-10-01",
          "value1": 44
        },
        {
          "date": "2025-09-01",
          "value1": 39
        },
        {
          "date": "2025-08-01",
          "value1": 34
        },
        {
          "date": "2025-07-01",
          "value1": 34
        },
        {
          "date": "2025-06-01",
          "value1": null
        }
      ]
    },
    {
      "relevancy": 0.87,
      "title": "Sleep Schedule Became More Irregular This Month",
      "description": "Your average bedtime and wake times fluctuated more this month, with a wider gap between earlier and later nights and mornings. Irregular sleep timing can confuse your natural circadian rhythm, making it harder to get deep, restorative rest and potentially leading to increased sleep debt and a drop in recovery signals like HRV.",
      "suggestions": "Try using a consistent pre-bed ritual to gently cue your body for sleep at a set time, even if life gets busy.",
      "related_suggested_questions": [
        "Do late bedtimes or inconsistent routines leave you feeling groggier?",
        "Did nights with earlier, steady bedtimes lead to higher scores or more deep sleep?",
        "Is your evening environment helping you unwind, or does it keep you wired?"
      ],
      "graph_type": "circadian_mid_point_month",
      "graph": [
        {
          "date": "2025-11-01",
          "value1": null
        },
        {
          "date": "2025-10-01",
          "value1": null
        },
        {
          "date": "2025-09-01",
          "value1": null
        },
        {
          "date": "2025-08-01",
          "value1": null
        },
        {
          "date": "2025-07-01",
          "value1": null
        },
        {
          "date": "2025-06-01",
          "value1": null
        }
      ]
    }
  ]
]
        """.trimIndent()

        val type = object : TypeToken<List<List<InsightItemResponseModel>>>() {}.type
        val raw = Gson().fromJson<List<List<InsightItemResponseModel>>>(jsonRes, type)
        val response = ArrayList<InsightItemResponseModel>()
        raw.forEach { insightsList ->
            response.addAll(insightsList)
        }
        val dummy = generateData(response)
        _insightsCardsData.value = dummy*/

        //--
        viewModelScope.launch {
            if(_insightsCardsData.value!=null){
                _insightsCardsData.value = _insightsCardsData.value
                return@launch
            }

            userRepository.getInsightLvl1List().collect{ resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        loadInsightsData()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            if(it.isNullOrEmpty()){
                                _insightsCardsData.value = emptyList()
                                return@let
                            }
                            val response = ArrayList<InsightItemResponseModel>()
                            it.forEach { insightsList ->
                                response.addAll(insightsList)
                            }
                            val dummy = generateData(response.take(3))
                            _insightsCardsData.value = dummy
                        }
                    }
                }
            }
        }
        //--

    }

    private fun generateData(data: List<InsightItemResponseModel>): List<InsightCardUiModel> {
        val list = ArrayList<InsightCardUiModel>()

        data.forEach {
            if (it.graph_type.isNullOrEmpty().not()) {
                when (it.graph_type) {
                    GraphType.Day.REM_SLEEP,
                    GraphType.Week.REM_SLEEP,
                    GraphType.Month.REM_SLEEP -> {
                        graphDataConvertor.generateSleepMultiBarChartData(
                            it, SleepInternalLaunchState.REM_SLEEP
                        )?.let { data ->
                            list.add(data)
                        }
                        // (double_bar_plot with Deep Sleep)
                    }

                    GraphType.Day.DEEP_SLEEP,
                    GraphType.Week.DEEP_SLEEP,
                    GraphType.Month.DEEP_SLEEP -> {
                        graphDataConvertor.generateSleepMultiBarChartData(
                            it, SleepInternalLaunchState.DEEP_SLEEP
                        )?.let { data ->
                            list.add(data)
                        }
                        // (double_bar_plot with REM Sleep)
                    }

                    GraphType.Day.SLEEP_EFFICIENCY,
                    GraphType.Week.SLEEP_EFFICIENCY,
                    GraphType.Month.SLEEP_EFFICIENCY -> {
                        graphDataConvertor.generateTrendsGraphInsightsData(it)?.let { data ->
                            list.add(data)
                        }
                        // (standard_bar_plot)
                    }

                    GraphType.Day.TOTAL_DURATION,
                    GraphType.Week.TOTAL_DURATION,
                    GraphType.Month.TOTAL_DURATION -> {
                        graphDataConvertor.generateTrendsGraphInsightsData(it)?.let { data ->
                            list.add(data)
                        }
                        // (standard_bar_plot)
                    }

                    GraphType.Day.LATENCY,
                    GraphType.Week.LATENCY,
                    GraphType.Month.LATENCY -> {
                        graphDataConvertor.generateTrendsGraphInsightsData(it)?.let { data ->
                            list.add(data)
                        }
                        // (standard_bar_plot)
                    }

                    GraphType.Day.RESTFULLNESS,
                    GraphType.Week.RESTFULLNESS,
                    GraphType.Month.RESTFULLNESS -> {
                        graphDataConvertor.getBarPlotColorData(
                            it,
                            SleepInternalLaunchState.RESTFULNESS,
                            "%"
                        )?.let { plotData ->
                            list.add(plotData)
                        }
                        // RESTFULNESS (bar_plot_color)
                    }

                    GraphType.Day.HRV,
                    GraphType.Week.HRV,
                    GraphType.Month.HRV -> {
                        graphDataConvertor.getBarPlotColorData(
                            it,
                            SleepInternalLaunchState.HRV,
                            "ms"
                        )?.let { plotData ->
                            list.add(plotData)
                        }
                        // HRV (bar_plot_color)
                    }

                    GraphType.Day.RHR,
                    GraphType.Week.RHR,
                    GraphType.Month.RHR -> {
                        graphDataConvertor.getBarPlotColorData(
                            it,
                            SleepInternalLaunchState.RESTING_HEART_RATE,
                            "bpm"
                        )?.let { plotData ->
                            list.add(plotData)
                        }
                        // RHR (bar_plot_color)
                    }

                    GraphType.Day.AVG_SKIN_TEMP,
                    GraphType.Week.AVG_SKIN_TEMP,
                    GraphType.Month.AVG_SKIN_TEMP -> {
                        graphDataConvertor.getBarPlotColorData(
                            it,
                            SleepInternalLaunchState.SKIN_TEMPERATURE,
                            "°C"
                        )?.let { plotData ->
                            list.add(plotData)
                        }
                        // SKIN_TEMP (bar_plot_color)
                    }

                    GraphType.Day.AVG_OXY,
                    GraphType.Week.AVG_OXY,
                    GraphType.Month.AVG_OXY -> {
                        list.add(
                            graphDataConvertor.generateSleepSingleLineChartData(
                                it,
                                getPeriod(it),
                                SleepInternalLaunchState.SKIN_TEMPERATURE
                            )
                        )
                        // SPO2 (line_plot)
                    }

                    GraphType.Day.AVG_RESPIRATION,
                    GraphType.Week.AVG_RESPIRATION,
                    GraphType.Month.AVG_RESPIRATION -> {
                        list.add(
                            graphDataConvertor.generateSleepSingleLineChartData(
                                it,
                                getPeriod(it),
                                getContributor(it)
                            )
                        )
                        // (line_plot)
                    }

                    GraphType.Day.CIRCADIAN_MID_POINT -> {
                        list.add(
                            graphDataConvertor.generateSleepTimingChartInternalData(
                                it,
                                getPeriod(it),
                                getContributor(it)
                            )
                        )
                    }

                    GraphType.Week.CIRCADIAN_MID_POINT,
                    GraphType.Month.CIRCADIAN_MID_POINT -> {
                        list.add(
                            graphDataConvertor.generateSleepSingleLineChartData(
                                it,
                                getPeriod(it),
                                getContributor(it)
                            )
                        )
                        // Week -> line_plot
                        // Month-> line_plot
                    }

                    else -> {
                        val data = graphDataConvertor.generateTrendsGraphInsightsData(it)
                        data?.let {
                            list.add(data)
                        }
                    }
                }
            }

        }


        // 5) Health monitor internal graphs (day-level gradient charts)
        val today = java.time.LocalDate.now()
        fun dates(n: Int) = (0 until n).map { i -> today.minusDays((n - 1 - i).toLong()) }

        fun sinSeries(
            n: Int,
            base: Float,
            amp: Float,
            clampMin: Float? = null,
            clampMax: Float? = null
        ): List<Float?> {
            return (0 until n).map { i ->
                val v = base + (amp * kotlin.math.sin(2 * kotlin.math.PI * i / n)).toFloat()
                val c1 = clampMin?.let { kotlin.math.max(v, it) } ?: v
                val c2 = clampMax?.let { kotlin.math.min(c1, it) } ?: c1
                c2
            }
        }

        // Respiratory rate – day (bar)
        /* list.add(
             InsightCardUiModel(
                 id = 5L,
                 title = "Respiratory rate (day demo)",
                 timeText = "day",
                 chartKey = "respiratory_day",
                 payload = TimeSeriesPayload(
                     values = sinSeries(7, 16f, 1.2f),
                     dates = dates(7),
                     unitLabel = "rpm"
                 )
             )
         )

         // Resting HR – week (line)
         list.add(
             InsightCardUiModel(
                 id = 6L,
                 title = "Resting HR (week demo)",
                 timeText = "week",
                 chartKey = "resting_hr_week",
                 payload = TimeSeriesPayload(
                     values = sinSeries(6, 62f, 6f),
                     dates = dates(6),
                     unitLabel = "bpm"
                 )
             )
         )

         // HRV – month (line)
         list.add(
             InsightCardUiModel(
                 id = 7L,
                 title = "HRV (month demo)",
                 timeText = "month",
                 chartKey = "hrv_month",
                 payload = TimeSeriesPayload(
                     values = sinSeries(6, 40f, 8f),
                     dates = dates(6),
                     unitLabel = "ms"
                 )
             )
         )

         // Skin temperature – day (gradient)
         list.add(
             InsightCardUiModel(
                 id = 8L,
                 title = "Skin temperature (day demo)",
                 timeText = "day",
                 chartKey = "skin_temp_day",
                 payload = TimeSeriesPayload(
                     values = sinSeries(7, 0.0f, 0.8f),
                     dates = dates(7),
                     unitLabel = "°",
                     chartType = SleepSingleGradientChartType.FLOAT
                 )
             )
         )

         // Blood oxygen – daily (gradient)
         list.add(
             InsightCardUiModel(
                 id = 9L,
                 title = "Blood oxygen (daily demo)",
                 timeText = "daily",
                 chartKey = "blood_oxygen_daily",
                 payload = TimeSeriesPayload(
                     values = sinSeries(48, 97f, 1.0f, 90f, 100f),
                     dates = dates(48),
                     unitLabel = "%",
                     chartType = SleepSingleGradientChartType.PERCENT
                 )
             )
         )*/

        return list
    }

    fun loadWhatsNew() {
        _whatsNew.value = LifeOsWhatsNewResponse(
            version = 1.2f,
            whatsNewList = listOf(
                resourcesProvider.getString(R.string.text_lifeos_whats_new_content_1),
                resourcesProvider.getString(R.string.text_lifeos_whats_new_content_2),
                resourcesProvider.getString(R.string.text_lifeos_whats_new_content_3),
            )
        )
    }

    fun getGreetText(): String {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val userFirstName = userRepository.getUser()?.firstName ?: "User"
        return when (currentHour) {
            in 4..11 -> resourcesProvider.getString(R.string.text_good_morning_val, userFirstName)
            in 12..16 -> resourcesProvider.getString(R.string.text_good_afternoon_val, userFirstName)
            in 17..21 -> resourcesProvider.getString(R.string.text_good_evening_val, userFirstName)
            else -> resourcesProvider.getString(R.string.text_hi_text, userFirstName)
            /*in 22..23, in 0..3 -> "Hi $userFirstName"*/
        }
    }

    private fun getPeriod(data: InsightItemResponseModel) = when {
        data.graph_type?.endsWith("week") == true -> InternalSelectedPeriod.WEEK
        data.graph_type?.endsWith("month") == true -> InternalSelectedPeriod.MONTH
        else -> InternalSelectedPeriod.DAY
    }

    private fun getContributor(data: InsightItemResponseModel): SleepInternalLaunchState {
        return when {
            data.graph_type == null -> SleepInternalLaunchState.DEEP_SLEEP
            data.graph_type.startsWith("circadian_mid_point_day") -> SleepInternalLaunchState.TIMING
            data.graph_type.startsWith("hour_vs_need_day") -> SleepInternalLaunchState.HOUR_VS_NEED
            data.graph_type.startsWith("restorative_sleep") -> SleepInternalLaunchState.RESTORATIVE_SLEEP
            data.graph_type.startsWith("rem_sleep") -> SleepInternalLaunchState.REM_SLEEP
            data.graph_type.startsWith("deep_sleep") -> SleepInternalLaunchState.DEEP_SLEEP
            data.graph_type.startsWith("sleep_perf") -> SleepInternalLaunchState.SLEEP_PERFORMANCE
            else -> SleepInternalLaunchState.DEEP_SLEEP
        }
    }

    enum class LifeOsDestinations{
        BEGIN_FRAG, QUES_FRAG, LIFE_OS_MAIN
    }
}
