package com.oreo.ui.lifeos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.dataConverter.GraphDataConvertor
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import com.oreo.data.model.lifeos.dashModels.LifeOsWhatsNewResponse
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.lifeos.charts.InsightCardUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LifeOsDashViewModel @Inject constructor(
    val localDataStore: DataStoredInterface,
    val graphDataConvertor: GraphDataConvertor,
    val oreoDeviceRepository: OreoDeviceRepository,
    private val userRepository: UserRepository,
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

            val isAllDone = onBoardData.questions?.size == onBoardData.answers?.size
            if(!isAllDone){
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
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
//                        setLoading(resource.loading)
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
                            val fata = it
                            _questions.value = it.questions?.map { it.question as String }
                            //showHistoryIcon.value = it.hasHistory
                        }
                    }
                }
            }
        }


    }

    fun loadInsightsData() {

        val jsonRes = """
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
        _insightsCardsData.value = dummy

        //--
        /*viewModelScope.launch {
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
                        resource.data?.data?.let {
                            if(it.isEmpty()){
                                _insightsCardsData.value = emptyList()
                                return@let
                            }
                            val response = ArrayList<InsightItemResponseModel>()
                            it.forEach { insightsList ->
                                response.addAll(insightsList)
                            }
                            val dummy = generateData(response)
                            _insightsCardsData.value = dummy
                        }
                    }
                }
            }
        }*/
        //--

    }

    private fun generateData(data: List<InsightItemResponseModel>): List<InsightCardUiModel> {
        val list = ArrayList<InsightCardUiModel>()

        data.forEach { it ->

            if (it.graph_type.isNullOrEmpty().not()) {
                when (it.graph_type) {
                    "hr" -> {
                        list.add(graphDataConvertor.generateHrGraphData(it))
                    }

                    "stress" -> {
                        list.add(graphDataConvertor.generateStressGraphData(it))
                    }

                    "daytime" -> {
                        list.add(graphDataConvertor.generateDayTimeGraphData(it))
                    }

                    "sleep_stage" -> {
                        list.add(graphDataConvertor.generateSleepBreakupGraphData(it))
                    }
                    "sleep_movement" -> {
                        list.add(graphDataConvertor.generateSleepMovementData(it))
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
                "<b>New Timeline:</b> streamlined logging for Supplements and Recovery",
                "<b>AI coaching improvements:</b> better context understanding and tips",
                "<b>Dashboard tweaks:</b> faster loading and refreshed visuals"
            )
        )
    }

    enum class LifeOsDestinations{
        BEGIN_FRAG, QUES_FRAG, LIFE_OS_MAIN
    }
}
