package com.oreo.ui.lifeos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit.data.remote.base.Resource
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
) : BaseViewModel() {

    private val _questions = MutableLiveData<List<String>>()
    val questions: LiveData<List<String>> get() = _questions

    private val _whatsNew = MutableLiveData<LifeOsWhatsNewResponse>()
    val whatsNew: LiveData<LifeOsWhatsNewResponse> get() = _whatsNew

    val destinationData = MutableLiveData<LifeOsDestinations?>()

    private val _insightsCardsData = MutableLiveData<List<InsightCardUiModel>>()
    val insightsCardsData: LiveData<List<InsightCardUiModel>> get() = _insightsCardsData

    init {
        loadSuggestedQuestions()
        loadWhatsNew()
        loadInsightsData()
    }

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
            }

            destinationData.postValue(LifeOsDestinations.LIFE_OS_MAIN)
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
                        setLoading(resource.loading)
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
  {
    "relevancy": 0.95,
    "title": "Total sleep increased 11% this month compared to last month",
    "description": "Your average nightly sleep time rose from 5h 38m last month to 6h 16m this month — an 11% increase. That change mostly came from a handful of longer sleeps (several 8+ hour nights) and later wake times this month, which gave you more opportunity to complete additional sleep cycles. More total sleep usually helps daytime energy and cognitive performance, but inconsistent timing (late mid-sleep times on some days) can blunt circadian benefits.",
    "suggestions": "Keep the longer total sleep by protecting wake time consistency — aim to wake within a 30-minute window most days while preserving the earlier bedtime that allowed extra sleep on longer nights.",
    "related_suggested_questions": [
      "Which days had the longest sleeps and what was different about your evening (workouts, alcohol, naps)?",
      "Did later wake times fall on weekends or recovery days?",
      "Did nights with extra sleep align with lower stress or lighter training days?"
    ],
    "graph_type": "total_duration_month",
    "graph": [
      {
        "date": "2025-06-01",
        "value1": null
      },
      {
        "date": "2025-07-01",
        "value1": null
      },
      {
        "date": "2025-08-01",
        "value1": 24720
      },
      {
        "date": "2025-09-01",
        "value1": 24727
      },
      {
        "date": "2025-10-01",
        "value1": 21200
      },
      {
        "date": "2025-11-01",
        "value1": 22583
      }
    ]
  },
  {
    "relevancy": 0.87,
    "title": "Deep sleep rose ~7% this month compared to last month",
    "description": "Your average deep sleep increased from 1h 21m last month to 1h 27m this month — about a 7% gain. Small increases like this often reflect better recovery habits (extra sleep opportunity, fewer late nights) or well-timed easier training days. Improved deep sleep supports physical recovery and strength gains.",
    "suggestions": "Keep one to two deliberate recovery evenings per week (low-intensity activity, earlier dinner, reduced alcohol) to help maintain deeper NREM sleep.",
    "related_suggested_questions": [
      "Were nights with more deep sleep preceded by lighter training or rest days?",
      "Did alcohol or late caffeine appear more often on nights with reduced deep sleep?",
      "Are your longest deep-sleep nights also the ones with earlier bedtimes or cooler sleep temps?"
    ],
    "graph_type": "deep_sleep_month",
    "graph": [
      {
        "date": "2025-06-01",
        "value1": null
      },
      {
        "date": "2025-07-01",
        "value1": null
      },
      {
        "date": "2025-08-01",
        "value1": 5880
      },
      {
        "date": "2025-09-01",
        "value1": 5640
      },
      {
        "date": "2025-10-01",
        "value1": 5158
      },
      {
        "date": "2025-11-01",
        "value1": 5223
      }
    ]
  },
  {
    "relevancy": 0.8,
    "title": "HRV stayed stable (about 51 ms) month‑over‑month",
    "description": "Your nightly average HRV this month was ~51 ms — essentially unchanged from the previous month (≈51 ms). That stability suggests your autonomic recovery has been steady: training load, sleep quality, and daily stressors appear roughly balanced overall, even with ups and downs in nightly sleep duration.",
    "suggestions": "Keep the habits that support steady HRV: consistent sleep timing, hydration, and a short nightly breathing or relaxation routine after heavier days.",
    "related_suggested_questions": [
      "Which weeks showed the highest HRV and what habits (sleep timing, reduced alcohol, lower stress) lined up then?",
      "Do big swings in total sleep or a few high-stress days correlate with short HRV dips?",
      "When HRV was higher, did you notice differences in perceived recovery or workout performance?"
    ],
    "graph_type": "hrv_month",
    "graph": [
      {
        "date": "2025-06-01",
        "value1": null
      },
      {
        "date": "2025-07-01",
        "value1": null
      },
      {
        "date": "2025-08-01",
        "value1": 43
      },
      {
        "date": "2025-09-01",
        "value1": 55
      },
      {
        "date": "2025-10-01",
        "value1": 48
      },
      {
        "date": "2025-11-01",
        "value1": 51
      }
    ]
  }
]
        """.trimIndent()

        val type = object : TypeToken<List<InsightItemResponseModel>>() {}.type
        val raw = Gson().fromJson<List<InsightItemResponseModel>>(jsonRes, type)
        val dummy = generateData(raw)
        _insightsCardsData.value = dummy

        //--
        /*viewModelScope.launch {
            userRepository.getInsightLvl1List("day").collect{ resource ->
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
                                        loadInsights()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            val dummy = generateData(it)
                            _cards.value = dummy
                        }
                    }
                }
            }
        }*/
        //--

        /*val dummy = generateData(arrayListOf(InsightItemResponseModel(
            graph_type = "hr",
            title = "Sleep midpoint shifted later by ~50 minutes over recent nights",
        ),
            InsightItemResponseModel(
                graph_type = "stress",
                title = "Deep sleep has decreased ~18% over the recent days",
            ),
            InsightItemResponseModel(
                graph_type = "daytime"
            ),
            InsightItemResponseModel(
                graph_type = "sleep_stage"
            ),
            InsightItemResponseModel(graph_type = "sleep_movement"),
            InsightItemResponseModel(graph_type = "rem_day"),
            ))
        _cards.value = dummy*/

    }

    private fun processInsightsData(rawListData: List<InsightItemResponseModel>){

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
