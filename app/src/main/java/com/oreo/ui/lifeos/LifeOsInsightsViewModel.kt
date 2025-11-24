package com.oreo.ui.lifeos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import com.oreo.ui.lifeos.charts.DayTimeChartPayload
import com.oreo.ui.lifeos.charts.HrChartPayload
import com.oreo.ui.lifeos.charts.InsightCardUiModel
import com.oreo.ui.lifeos.charts.SleepChartPayload
import com.oreo.ui.lifeos.charts.StressChartPayload
import com.oreo.ui.lifeos.charts.TimeSeriesPayload
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientChartType
import com.oreo.ui.custom.HRCombineModel
import com.oreo.ui.custom.StressCombineModel
import com.noisefit.luna.R
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.models.SleepData
import kotlin.math.sin
import kotlin.math.PI
import java.util.ArrayList
import android.graphics.Color
import com.oreo.data.dataConverter.OreoHRDataConvertor
import com.oreo.ui.lifeos.charts.GraphConvertorUtil
import com.oreo.data.model.DayTimeDataModel as DTModel
import com.oreo.data.model.Item as DTItem
import com.oreo.data.model.Section as DTSection
import com.oreo.ui.custom.Item as ChartItem
import com.oreo.ui.custom.Section as ChartSection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LifeOsInsightsViewModel @Inject constructor(
    val hrDataConvertor: OreoHRDataConvertor,
) : ViewModel() {

    private val _insights = MutableLiveData<List<InsightItemResponseModel>>()
    private val _cards = MutableLiveData<List<InsightCardUiModel>>()
    val cards: LiveData<List<InsightCardUiModel>> get() = _cards

    init {
        loadInsights()
    }

    fun loadInsights() {

        val jsonRes = """
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
                "graphs": "hrv",
                "graph": [
                  {
                    "date": "2025-11-09",
                    "master_avg_hrv": 47
                  },
                  {
                    "date": "2025-11-10",
                    "master_avg_hrv": 70
                  },
                  {
                    "date": "2025-11-11",
                    "master_avg_hrv": 36
                  },
                  {
                    "date": "2025-11-12",
                    "master_avg_hrv": 50
                  },
                  {
                    "date": "2025-11-13",
                    "master_avg_hrv": 50
                  },
                  {
                    "date": "2025-11-14",
                    "master_avg_hrv": 51
                  },
                  {
                    "date": "2025-11-15",
                    "master_avg_hrv": 50
                  },
                  {
                    "date": "2025-11-16",
                    "master_avg_hrv": 40
                  },
                  {
                    "date": "2025-11-17",
                    "master_avg_hrv": 35
                  }
                ]
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
                "graphs": "deep_sleep",
                "graph": [
                  {
                    "date": "2025-11-09",
                    "master_deep": 10770
                  },
                  {
                    "date": "2025-11-10",
                    "master_deep": 6780
                  },
                  {
                    "date": "2025-11-11",
                    "master_deep": 9060
                  },
                  {
                    "date": "2025-11-12",
                    "master_deep": 5280
                  },
                  {
                    "date": "2025-11-13",
                    "master_deep": null
                  },
                  {
                    "date": "2025-11-14",
                    "master_deep": 7050
                  },
                  {
                    "date": "2025-11-15",
                    "master_deep": 5460
                  },
                  {
                    "date": "2025-11-16",
                    "master_deep": 4770
                  },
                  {
                    "date": "2025-11-17",
                    "master_deep": 5460
                  }
                ]
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
                "graphs": "circadian_mid_point",
                "graph": [
                  {
                    "date": "2025-11-09",
                    "master_mid_time": "04:49:30"
                  },
                  {
                    "date": "2025-11-10",
                    "master_mid_time": "03:14:00"
                  },
                  {
                    "date": "2025-11-11",
                    "master_mid_time": "04:46:30"
                  },
                  {
                    "date": "2025-11-12",
                    "master_mid_time": "03:30:00"
                  },
                  {
                    "date": "2025-11-13",
                    "master_mid_time": "02:30:00"
                  },
                  {
                    "date": "2025-11-14",
                    "master_mid_time": "04:28:00"
                  },
                  {
                    "date": "2025-11-15",
                    "master_mid_time": "04:57:30"
                  },
                  {
                    "date": "2025-11-16",
                    "master_mid_time": "04:33:00"
                  },
                  {
                    "date": "2025-11-17",
                    "master_mid_time": "03:31:30"
                  }
                ]
              }
            ]
        """.trimIndent()

        val type = object : TypeToken<List<InsightItemResponseModel>>() {}.type
        val raw = Gson().fromJson<List<InsightItemResponseModel>>(jsonRes, type)
        //_insights.value = raw

        val dummy = buildDummyCards()
        _cards.value = dummy

    }

    private fun buildDummyCards(): List<InsightCardUiModel> {
        val list = ArrayList<InsightCardUiModel>()

        val hrItems = ArrayList<Int>()
        for (i in 0 until 288) {
            val v = (50..150).random()
            hrItems.add(v)
        }

        val hrData =  GraphConvertorUtil.parseHrData(hrItems)
        val combinedHrData  = hrDataConvertor.getHrCombinedData(
            null,hrData
        )


        list.add(
            InsightCardUiModel(
                id = 1L,
                title = "Heart rate demo",
                timeText = "now",
                chartKey = "hr",
                payload = HrChartPayload(combinedHrData, yAxisCount = 5, minYAxis = hrData.minValues,
                    maxYAxis = hrData.maxValues),
                styleRes = R.style.HrChartStyle,
                raw = null
            )
        )



        // 2) Stress chart dummy
        val stressItems = ArrayList<ChartItem>()
        for (i in 0 until 96) {
            val v = (30..100).random()
            stressItems.add(ChartItem(value = v, index = i, minValue = 0, maxValue = 100))
        }

        val stressModel = StressCombineModel(sections = arrayListOf(), items = stressItems, high = 75, medium = 50)
        list.add(
            InsightCardUiModel(
                id = 2L,
                title = "Stress demo",
                timeText = "today",
                chartKey = "stress",
                payload = StressChartPayload(stressModel),
                styleRes = R.style.StressChartStyle,
                raw = null
            )
        )

        // 3) Daytime activity dummy
        val dtItems = ArrayList<DTItem>()
        for (i in 0 until 48) {
            val v = when {
                i in 0..5 -> 0 // inactive
                i in 6..10 -> 1 // low
                i in 11..20 -> 2 // medium
                i in 21..25 -> 3 // high
                i in 26..30 -> 2
                i in 31..40 -> 1
                else -> 0
            }
            dtItems.add(DTItem(value = v, index = i))
        }
        val dtSections = listOf(
            DTSection(type = "sleep", start = 0, end = 6, color = Color.parseColor("#33224460"), imageRes = R.drawable.image_blur_avg),
            DTSection(type = "nap", start = 28, end = 30, color = Color.parseColor("#33406080"), imageRes = R.drawable.image_blur_avg)
        )
        val dtModel = DTModel(sections = dtSections, items = dtItems)
        list.add(
            InsightCardUiModel(
                id = 3L,
                title = "Daytime movement demo",
                timeText = "this week",
                chartKey = "daytime",
                payload = DayTimeChartPayload(dtModel),
                styleRes = R.style.DayTimeGraphStyle,
                raw = null
            )
        )

        // 4) Sleep dummy
        val totals = CountCardData(
            count = "7h 20m",
            type = "sleep",
            countSubText = "duration"
        )
        val breakup = arrayListOf<SleepData.SleepDataBreakup>().apply {
            add(SleepData.SleepDataBreakup(startTime = "23:00", endTime = "00:00", sleepType = "light", duration = 60))
            add(SleepData.SleepDataBreakup(startTime = "00:00", endTime = "01:30", sleepType = "deep", duration = 90))
            add(SleepData.SleepDataBreakup(startTime = "01:30", endTime = "02:00", sleepType = "awake", duration = 30))
            add(SleepData.SleepDataBreakup(startTime = "02:00", endTime = "03:00", sleepType = "rem", duration = 60))
            add(SleepData.SleepDataBreakup(startTime = "03:00", endTime = "06:20", sleepType = "light", duration = 200))
        }
        list.add(
            InsightCardUiModel(
                id = 4L,
                title = "Sleep analysis demo",
                timeText = "last night",
                chartKey = "sleep",
                payload = SleepChartPayload(totals = totals, breakup = breakup, interactive = true),
                raw = null
            )
        )

        // 5) Health monitor internal graphs (day-level gradient charts)
        val today = java.time.LocalDate.now()
        fun dates(n: Int) = (0 until n).map { i -> today.minusDays((n - 1 - i).toLong()) }

        fun sinSeries(n: Int, base: Float, amp: Float, clampMin: Float? = null, clampMax: Float? = null): List<Float?> {
            return (0 until n).map { i ->
                val v = base + (amp * kotlin.math.sin(2 * kotlin.math.PI * i / n)).toFloat()
                val c1 = clampMin?.let { kotlin.math.max(v, it) } ?: v
                val c2 = clampMax?.let { kotlin.math.min(c1, it) } ?: c1
                c2
            }
        }

        // Respiratory rate – day (bar)
        list.add(InsightCardUiModel(
            id = 5L,
            title = "Respiratory rate (day demo)",
            timeText = "day",
            chartKey = "respiratory_day",
            payload = TimeSeriesPayload(
                values = sinSeries(7, 16f, 1.2f),
                dates = dates(7),
                unitLabel = "rpm"
            )
        ))

        // Resting HR – week (line)
        list.add(InsightCardUiModel(
            id = 6L,
            title = "Resting HR (week demo)",
            timeText = "week",
            chartKey = "resting_hr_week",
            payload = TimeSeriesPayload(
                values = sinSeries(6, 62f, 6f),
                dates = dates(6),
                unitLabel = "bpm"
            )
        ))

        // HRV – month (line)
        list.add(InsightCardUiModel(
            id = 7L,
            title = "HRV (month demo)",
            timeText = "month",
            chartKey = "hrv_month",
            payload = TimeSeriesPayload(
                values = sinSeries(6, 40f, 8f),
                dates = dates(6),
                unitLabel = "ms"
            )
        ))

        // Skin temperature – day (gradient)
        list.add(InsightCardUiModel(
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
        ))

        // Blood oxygen – daily (gradient)
        list.add(InsightCardUiModel(
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
        ))

        return list
    }
}
