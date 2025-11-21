package com.oreo.ui.lifeos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LifeOsInsightsViewModel @Inject constructor() : ViewModel() {

    private val _insights = MutableLiveData<List<InsightItemResponseModel>>()
    val insights: LiveData<List<InsightItemResponseModel>> get() = _insights

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
        _insights.value = Gson().fromJson(jsonRes, type)

    }
}
