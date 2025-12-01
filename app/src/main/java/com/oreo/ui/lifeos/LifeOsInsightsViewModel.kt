package com.oreo.ui.lifeos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import androidx.lifecycle.ViewModel
import com.oreo.data.dataConverter.GraphDataConvertor
import com.oreo.data.dataConverter.OreoHRDataConvertor
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import com.oreo.ui.lifeos.charts.InsightCardUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LifeOsInsightsViewModel @Inject constructor(
    val hrDataConvertor: OreoHRDataConvertor,
    val graphDataConvertor: GraphDataConvertor,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    private val _insights = MutableLiveData<List<InsightItemResponseModel>>()
    private val _cards = MutableLiveData<List<InsightCardUiModel>>()
    val cards: LiveData<List<InsightCardUiModel>> get() = _cards

    init {
        loadInsights()
    }

    fun loadInsights() {

        /*val jsonRes = """
            [
              {
                "relevancy": 0.95,
                "title": "HRV dropped ~37% last night",
                "description": "Your average sleep HRV fell from ~56 ms (prior week average) to 35 ms on 2025-11-17 — a ~37% drop. That large decline suggests reduced physiological recovery overnight (could reflect higher daytime stress, recent training load, illness, or alcohol/caffeine late in the day). Your readiness score also fell to 54 the same day, which supports the idea your body felt less recovered despite an OK sleep duration.",
                "suggestions":"Prioritize an easy/recovery day today — avoid high-intensity training until HRV recovers.",
                "related_suggested_questions": [
                  "Did you feel more stressed or have unusual symptoms on 2025-11-16/17?",
                  "Was there alcohol, extra caffeine, or a late heavy meal the night before the HRV drop?",
                  "How does this HRV drop compare to other low-readiness days in the past month?"
                ],
                "graph_type": "hrv",
              
              },
              {
                "relevancy": 0.88,
                "title": "Deep sleep has decreased ~18% over the recent days",
                "description": "Your deep sleep percentage averaged ~23.7% across earlier nights (Nov 9–13/14) but has fallen to ~19% in the most recent 3 nights (Nov 14–16) — about an 18% relative decrease. Less deep sleep can reduce physical recovery and explain lower HRV/readiness even when total sleep time looks adequate.",
                "suggestions": "Keep a consistent wind‑down routine and aim for the same bedtime each night to support deeper sleep stages.",
                "related_suggested_questions": [
                  "Did late evening activity, caffeine, or alcohol increase on nights with lower deep sleep?",
                  "Are nights with lower deep sleep followed by higher resting HR or lower HRV the next day?",
                  "Does total sleep time or sleep fragmentation (awake minutes) differ on your deepest nights?"
                ],
                "graph_type": "deep_sleep",
                
              },
              {
                "relevancy": 0.82,
                "title": "Sleep midpoint shifted later by ~50 minutes over recent nights",
                "description": "Comparing recent 3-night windows, your average sleep midpoint moved later by roughly 50 minutes (recent nights average ~4:20 AM vs earlier window ~3:30 AM). That variability in sleep timing can fragment sleep architecture and affect recovery and daytime alertness.",
                "suggestions": "Try to keep your bedtime and wake time within a 30‑minute window across the week (including weekends).",
                "related_suggested_questions": [
                  "Which nights had the latest bedtimes or wake times that drove this shift?",
                  "Did nights with later midpoints have less deep sleep or lower HRV the next day?",
                  "Are evening screen use, late caffeine, or social schedules correlating with these later midpoints?"
                ],
                "graph_type": "circadian_mid_point",
              }
            ]
        """.trimIndent()

        val type = object : TypeToken<List<InsightItemResponseModel>>() {}.type
        val raw = Gson().fromJson<List<InsightItemResponseModel>>(jsonRes, type)
        //_insights.value = raw*/

        //--
        viewModelScope.launch {
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
        }
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
                        val data = graphDataConvertor.handleTrendsData(it)
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

}
