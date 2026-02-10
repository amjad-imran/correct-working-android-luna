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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit.session.SessionManager
import com.noisefit_commans.utils.GraphType
import com.oreo.data.dataConverter.GraphDataConvertor
import com.oreo.data.dataConverter.OreoHRDataConvertor
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import com.oreo.ui.lifeos.charts.InsightCardUiModel
import com.oreo.ui.sleep2.internal.InternalSelectedPeriod
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LifeOsInsightsViewModel @Inject constructor(
    val hrDataConvertor: OreoHRDataConvertor,
    val graphDataConvertor: GraphDataConvertor,
    private val userRepository: UserRepository,
    val sessionManager: SessionManager,
) : BaseViewModel() {

    private val _cards = MutableLiveData<List<InsightCardUiModel>>()
    val cards: LiveData<List<InsightCardUiModel>> get() = _cards

    init {
        loadInsights()
    }

    fun loadInsights() {

        /*val jsonRes = """
            [
    [
      {
        "relevancy": 0.9,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Improvement in REM Sleep Duration",
        "description": "Your REM sleep increased to 1h 15m yesterday. REM sleep is crucial for emotional processing and memory consolidation. This increase might help improve cognitive functions and emotional resilience.",
        "suggestions": "Consider sticking to this schedule and ensuring a relaxing bedtime routine to maintain these benefits.",
        "related_suggested_questions": [
          "What activities can enhance my REM sleep?",
          "How does REM sleep affect my mood and memory?",
          "Did my REM sleep increase last week as well?"
        ],
        "graph_type": "rem_sleep_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": null
          },
          {
            "date": "2025-12-06",
            "value1": 4140
          },
          {
            "date": "2025-12-07",
            "value1": 4500
          },
          {
            "date": "2025-12-08",
            "value1": null
          },
          {
            "date": "2025-12-09",
            "value1": 6660
          },
          {
            "date": "2025-12-10",
            "value1": null
          },
          {
            "date": "2025-12-11",
            "value1": 4470
          }
        ]
      },
      {
        "relevancy": 0.88,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Deep Sleep Levels Enhanced",
        "description": "Yesterday, you experienced 1h 28m of deep sleep, which is an increase from your recent averages. Deep sleep is key to physical recovery and muscle repair.",
        "suggestions": "To promote deep sleep, maintain a regular sleep schedule and reduce caffeine intake in the afternoon.",
        "related_suggested_questions": [
          "What role does deep sleep play in recovery?",
          "How consistent are my deep sleep patterns?",
          "Should I adjust my evening activities for better deep sleep?"
        ],
        "graph_type": "deep_sleep_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": null
          },
          {
            "date": "2025-12-06",
            "value1": 7260
          },
          {
            "date": "2025-12-07",
            "value1": 6090
          },
          {
            "date": "2025-12-08",
            "value1": null
          },
          {
            "date": "2025-12-09",
            "value1": 5370
          },
          {
            "date": "2025-12-10",
            "value1": null
          },
          {
            "date": "2025-12-11",
            "value1": 5280
          }
        ]
      },
      {
        "relevancy": 0.86,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "High Sleep Efficiency Observed",
        "description": "Your sleep efficiency was 97%, indicating highly restorative sleep. Efficient sleep helps you wake up feeling refreshed and ready for the day.",
        "suggestions": "Try maintaining this level by avoiding large meals or intense exercise right before bedtime.",
        "related_suggested_questions": [
          "How does sleep efficiency relate to quality rest?",
          "What factors influence changes in sleep efficiency?",
          "Is my sleep efficiency affected by my daily activities?"
        ],
        "graph_type": "sleep_efficiency_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": null
          },
          {
            "date": "2025-12-06",
            "value1": 95
          },
          {
            "date": "2025-12-07",
            "value1": 97
          },
          {
            "date": "2025-12-08",
            "value1": null
          },
          {
            "date": "2025-12-09",
            "value1": 97
          },
          {
            "date": "2025-12-10",
            "value1": null
          },
          {
            "date": "2025-12-11",
            "value1": 97
          }
        ]
      },
      {
        "relevancy": 0.89,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Total Sleep Duration Increased",
        "description": "You slept for 7h 48m yesterday, which is a significant improvement. Longer sleep durations contribute to overall health and well-being.",
        "suggestions": "Aim for a consistent sleep schedule by going to bed and waking up at the same times daily.",
        "related_suggested_questions": [
          "How does total sleep duration affect recovery?",
          "What is the ideal amount of sleep for adults?",
          "Does my sleep duration affect my daily energy levels?"
        ],
        "graph_type": "total_duration_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": 25200
          },
          {
            "date": "2025-12-06",
            "value1": 30270
          },
          {
            "date": "2025-12-07",
            "value1": 28080
          },
          {
            "date": "2025-12-08",
            "value1": 25200
          },
          {
            "date": "2025-12-09",
            "value1": 30960
          },
          {
            "date": "2025-12-10",
            "value1": 14400
          },
          {
            "date": "2025-12-11",
            "value1": 28080
          }
        ]
      },
      {
        "relevancy": 0.7,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Shorter Sleep Latency Noted",
        "description": "Falling asleep took less time yesterday. Faster sleep onset can promote better rest and recovery.",
        "suggestions": "Stick to a calming bedtime routine to help maintain quick sleep onset.",
        "related_suggested_questions": [
          "What are ways to fall asleep faster?",
          "Does screen time impact sleep latency?",
          "Why is it important to reduce sleep latency?"
        ],
        "graph_type": "latency_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": null
          },
          {
            "date": "2025-12-06",
            "value1": 10
          },
          {
            "date": "2025-12-07",
            "value1": 6
          },
          {
            "date": "2025-12-08",
            "value1": null
          },
          {
            "date": "2025-12-09",
            "value1": 5
          },
          {
            "date": "2025-12-10",
            "value1": null
          },
          {
            "date": "2025-12-11",
            "value1": 6
          }
        ]
      },
      {
        "relevancy": 0.75,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Increased Restfulness During Sleep",
        "description": "Your restfulness showed improvement, indicating fewer disturbances during sleep. This can contribute to feeling more rejuvenated in the morning.",
        "suggestions": "Try using blackout curtains or a white noise machine to maintain this level of restfulness.",
        "related_suggested_questions": [
          "What causes disturbances during sleep?",
          "How does restfulness during sleep impact my day?",
          "Should I consider changing my sleep environment?"
        ],
        "graph_type": "restfullness_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": null
          },
          {
            "date": "2025-12-06",
            "value1": 8
          },
          {
            "date": "2025-12-07",
            "value1": 3
          },
          {
            "date": "2025-12-08",
            "value1": null
          },
          {
            "date": "2025-12-09",
            "value1": 5
          },
          {
            "date": "2025-12-10",
            "value1": null
          },
          {
            "date": "2025-12-11",
            "value1": 5
          }
        ]
      },
      {
        "relevancy": 0.83,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Balanced Heart Rate Variability (HRV)",
        "description": "Your HRV was at 21ms, indicating a steady balance. Consistent HRV can signify good autonomic nervous system health and stress management.",
        "suggestions": "Continue stress-reducing practices like mindfulness or moderate exercise to maintain this balance.",
        "related_suggested_questions": [
          "What does HRV tell me about my health?",
          "How does stress impact my HRV?",
          "Are there ways to improve my HRV readings?"
        ],
        "graph_type": "hrv_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": null
          },
          {
            "date": "2025-12-06",
            "value1": 50
          },
          {
            "date": "2025-12-07",
            "value1": 40
          },
          {
            "date": "2025-12-08",
            "value1": null
          },
          {
            "date": "2025-12-09",
            "value1": 40
          },
          {
            "date": "2025-12-10",
            "value1": null
          },
          {
            "date": "2025-12-11",
            "value1": 21
          }
        ]
      },
      {
        "relevancy": 0.76,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Resting Heart Rate (RHR) Remains Steady",
        "description": "Your RHR remained at 81 bpm, indicating a stable cardiovascular baseline. A normal RHR is crucial for cardiovascular health.",
        "suggestions": "To keep your heart healthy, engage in regular physical activity and monitor your nutrition intake.",
        "related_suggested_questions": [
          "What are the health implications of my resting heart rate?",
          "How does exercise affect my resting heart rate?",
          "What can I do to improve my cardiovascular health?"
        ],
        "graph_type": "rhr_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": null
          },
          {
            "date": "2025-12-06",
            "value1": 76
          },
          {
            "date": "2025-12-07",
            "value1": 78
          },
          {
            "date": "2025-12-08",
            "value1": null
          },
          {
            "date": "2025-12-09",
            "value1": 77
          },
          {
            "date": "2025-12-10",
            "value1": null
          },
          {
            "date": "2025-12-11",
            "value1": 81
          }
        ]
      },
      {
        "relevancy": 0.7,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Normal Average Skin Temperature",
        "description": "Your average skin temperature remained stable at 96°F. Consistent skin temperature can be a sign of balanced body regulation.",
        "suggestions": "Stay hydrated and avoid extreme temperatures to keep your internal balance intact.",
        "related_suggested_questions": [
          "How is skin temperature linked to sleep quality?",
          "What factors can cause fluctuations in skin temperature?",
          "Should I be concerned if my skin temperature changes?"
        ],
        "graph_type": "avg_skin_temp_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": null
          },
          {
            "date": "2025-12-06",
            "value1": 97.4
          },
          {
            "date": "2025-12-07",
            "value1": 97.1
          },
          {
            "date": "2025-12-08",
            "value1": null
          },
          {
            "date": "2025-12-09",
            "value1": 97
          },
          {
            "date": "2025-12-10",
            "value1": null
          },
          {
            "date": "2025-12-11",
            "value1": 95.5
          }
        ]
      },
      {
        "relevancy": 0.85,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Optimal Oxygen Saturation Maintained",
        "description": "Your average oxygen saturation was 97%, indicating good respiratory function. This is essential for efficient oxygen transport in your body.",
        "suggestions": "Maintain this level by engaging in regular physical activity and avoiding smoking or polluted areas.",
        "related_suggested_questions": [
          "What is a normal range for oxygen saturation?",
          "How can exercise improve my oxygen levels?",
          "What can a drop in oxygen saturation indicate?"
        ],
        "graph_type": "avg_oxy_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": null
          },
          {
            "date": "2025-12-06",
            "value1": 96
          },
          {
            "date": "2025-12-07",
            "value1": 96
          },
          {
            "date": "2025-12-08",
            "value1": null
          },
          {
            "date": "2025-12-09",
            "value1": 96
          },
          {
            "date": "2025-12-10",
            "value1": null
          },
          {
            "date": "2025-12-11",
            "value1": 97
          }
        ]
      },
      {
        "relevancy": 0.78,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Consistent Respiratory Rate Observed",
        "description": "Your average respiratory rate of 17 breaths per minute remains consistent. This stability supports effective breathing and oxygen delivery.",
        "suggestions": "Practice deep-breathing exercises to continue supporting your respiratory health.",
        "related_suggested_questions": [
          "What affects my respiratory rate changes?",
          "Is my current respiratory rate normal?",
          "How can respiratory exercises benefit me?"
        ],
        "graph_type": "avg_respiration_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": null
          },
          {
            "date": "2025-12-06",
            "value1": 16
          },
          {
            "date": "2025-12-07",
            "value1": 16
          },
          {
            "date": "2025-12-08",
            "value1": null
          },
          {
            "date": "2025-12-09",
            "value1": 16
          },
          {
            "date": "2025-12-10",
            "value1": null
          },
          {
            "date": "2025-12-11",
            "value1": 17
          }
        ]
      },
      {
        "relevancy": 0.72,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Circadian Midpoint Aligned",
        "description": "Your circadian midpoint was at 4:28 AM, showing alignment with your natural rhythm. Staying synced with your circadian rhythm helps with restorative sleep cycles.",
        "suggestions": "Keep a stable sleep schedule, even on weekends, to preserve your circadian alignment.",
        "related_suggested_questions": [
          "What is a circadian midpoint, and why is it important?",
          "How can I adjust my sleep schedule to improve my circadian rhythm?",
          "What happens if my circadian rhythm is disrupted?"
        ],
        "graph_type": "circadian_mid_point_day",
        "graph": [
          {
            "date": "2025-12-05",
            "value1": 12600
          },
          {
            "date": "2025-12-06",
            "value1": 19830
          },
          {
            "date": "2025-12-07",
            "value1": 17490
          },
          {
            "date": "2025-12-08",
            "value1": 16200
          },
          {
            "date": "2025-12-09",
            "value1": 13980
          },
          {
            "date": "2025-12-10",
            "value1": 10800
          },
          {
            "date": "2025-12-11",
            "value1": 16080
          }
        ]
      }
    ],
    [
      {
        "relevancy": 0.8,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "REM Sleep Decreased by 5%",
        "description": "Your REM sleep averaged 1h 22m this week, which is a slight decrease compared to the previous week's 1h 26m. REM sleep is crucial for emotional regulation and memory consolidation, so this dip might impact mood stability and cognitive functions.",
        "suggestions": "Prioritize a consistent sleep schedule and limit screen exposure before bed to boost REM sleep.",
        "related_suggested_questions": [
          "What factors affect REM sleep?",
          "How does stress impact REM sleep?",
          "What activities promote REM sleep?"
        ],
        "graph_type": "rem_sleep_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 5055
          },
          {
            "date": "2025-11-03",
            "value1": 4335
          },
          {
            "date": "2025-11-10",
            "value1": 5076
          },
          {
            "date": "2025-11-17",
            "value1": 5832
          },
          {
            "date": "2025-11-24",
            "value1": 3594
          },
          {
            "date": "2025-12-01",
            "value1": 4860
          }
        ]
      },
      {
        "relevancy": 0.85,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Deep Sleep Boosted by 12%",
        "description": "This week, your deep sleep increased to an average of 1h 47m from last week's 1h 35m. Deep sleep is critical for physical recovery and growth, suggesting your body is benefiting from enhanced muscle repair and immune function.",
        "suggestions": "Maintain a cool and dark bedroom environment to continue supporting deep sleep increases.",
        "related_suggested_questions": [
          "How does deep sleep affect muscle recovery?",
          "What can hinder deep sleep?",
          "How does diet influence deep sleep?"
        ],
        "graph_type": "deep_sleep_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 6098
          },
          {
            "date": "2025-11-03",
            "value1": 3885
          },
          {
            "date": "2025-11-10",
            "value1": 4398
          },
          {
            "date": "2025-11-17",
            "value1": 5568
          },
          {
            "date": "2025-11-24",
            "value1": 5280
          },
          {
            "date": "2025-12-01",
            "value1": 5808
          }
        ]
      },
      {
        "relevancy": 0.9,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Sleep Efficiency Increased by 3%",
        "description": "Your sleep efficiency improved to 95% this week from 92% last week, meaning you're spending more of your bedtime in restful sleep. Better sleep efficiency leads to more restorative rest and higher daily energy levels.",
        "suggestions": "Focus on relaxing activities before bed, such as reading or meditation, to keep up this trend.",
        "related_suggested_questions": [
          "What is considered good sleep efficiency?",
          "How can stress affect sleep efficiency?",
          "Why is sleep efficiency important?"
        ],
        "graph_type": "sleep_efficiency_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 94
          },
          {
            "date": "2025-11-03",
            "value1": 97
          },
          {
            "date": "2025-11-10",
            "value1": 94
          },
          {
            "date": "2025-11-17",
            "value1": 95
          },
          {
            "date": "2025-11-24",
            "value1": 96
          },
          {
            "date": "2025-12-01",
            "value1": 96
          }
        ]
      },
      {
        "relevancy": 0.7,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Total Sleep Duration Slightly Increased",
        "description": "Your weekly average for sleep duration rose to 7h 40m from 7h 35m last week. Consistent sleep duration supports overall well-being and improves cognitive function.",
        "suggestions": "Continue keeping a regular bedtime to maintain or increase your total sleep duration.",
        "related_suggested_questions": [
          "How much sleep is optimal for adults?",
          "How does sleep duration impact productivity?",
          "Can short-term sleep increase affect health?"
        ],
        "graph_type": "total_duration_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 25800
          },
          {
            "date": "2025-11-03",
            "value1": 24471
          },
          {
            "date": "2025-11-10",
            "value1": 27334
          },
          {
            "date": "2025-11-17",
            "value1": 30525
          },
          {
            "date": "2025-11-24",
            "value1": 24150
          },
          {
            "date": "2025-12-01",
            "value1": 27364
          }
        ]
      },
      {
        "relevancy": 0.75,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Improvement in Sleep Latency",
        "description": "Sleep latency improved to 6 minutes from 8 minutes last week, meaning you’re falling asleep faster. Faster sleep onset can be indicative of good sleep hygiene and reduced anxiety before bed.",
        "suggestions": "Keep up with pre-bedtime relaxation techniques, such as breathing exercises.",
        "related_suggested_questions": [
          "What affects sleep latency?",
          "How can I reduce sleep latency?",
          "What role does caffeine play in sleep latency?"
        ],
        "graph_type": "latency_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 7
          },
          {
            "date": "2025-11-03",
            "value1": 7
          },
          {
            "date": "2025-11-10",
            "value1": 5
          },
          {
            "date": "2025-11-17",
            "value1": 8
          },
          {
            "date": "2025-11-24",
            "value1": 5
          },
          {
            "date": "2025-12-01",
            "value1": 7
          }
        ]
      },
      {
        "relevancy": 0.6,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Consistent Restfulness Levels",
        "description": "Your perceived restfulness showed no significant change this week, remaining steady at 3 out of 5. Steady restfulness indicates a stable sleep pattern but exploring other sleep quality metrics could be beneficial.",
        "suggestions": "Track the activities preceding sleep on days you feel most rested to identify beneficial habits.",
        "related_suggested_questions": [
          "How does restfulness affect daily performance?",
          "Can diet impact restfulness during sleep?",
          "What is the relationship between restfulness and REM sleep?"
        ],
        "graph_type": "restfullness_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 5
          },
          {
            "date": "2025-11-03",
            "value1": 3
          },
          {
            "date": "2025-11-10",
            "value1": 4
          },
          {
            "date": "2025-11-17",
            "value1": 3
          },
          {
            "date": "2025-11-24",
            "value1": 5
          },
          {
            "date": "2025-12-01",
            "value1": 4
          }
        ]
      },
      {
        "relevancy": 0.92,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "HRV Increased by 14%",
        "description": "Your average HRV went up to 43ms from last week's 37ms. This boost suggests better cardiovascular health and improved stress management.",
        "suggestions": "Continue any stress-reducing practices you've been implementing, like yoga or mindful breathing.",
        "related_suggested_questions": [
          "How does HRV reflect stress and recovery?",
          "Can HRV predict changes in health?",
          "What lifestyle changes can improve HRV?"
        ],
        "graph_type": "hrv_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 43
          },
          {
            "date": "2025-11-03",
            "value1": 30
          },
          {
            "date": "2025-11-10",
            "value1": 31
          },
          {
            "date": "2025-11-17",
            "value1": 30
          },
          {
            "date": "2025-11-24",
            "value1": 36
          },
          {
            "date": "2025-12-01",
            "value1": 38
          }
        ]
      },
      {
        "relevancy": 0.65,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Resting Heart Rate Remained Steady",
        "description": "Your resting heart rate held steady at 78 bpm this week. A stable resting heart rate is an indicator of consistent cardiovascular fitness levels.",
        "suggestions": "Maintain regular physical activity to support heart health.",
        "related_suggested_questions": [
          "What is an optimal resting heart rate for adults?",
          "How does sleep affect resting heart rate?",
          "What can cause fluctuations in resting heart rate?"
        ],
        "graph_type": "rhr_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 77
          },
          {
            "date": "2025-11-03",
            "value1": 81
          },
          {
            "date": "2025-11-10",
            "value1": 80
          },
          {
            "date": "2025-11-17",
            "value1": 80
          },
          {
            "date": "2025-11-24",
            "value1": 76
          },
          {
            "date": "2025-12-01",
            "value1": 78
          }
        ]
      },
      {
        "relevancy": 0.68,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Slight Increase in Average Skin Temperature",
        "description": "Your average skin temperature rose to 97.2°F from last week's 97°F. Small fluctuations in skin temperature can be normal, but consistent rises could indicate stress or inflammation.",
        "suggestions": "Monitor hydration levels and ensure a balanced diet to manage skin temperature changes.",
        "related_suggested_questions": [
          "What factors affect skin temperature?",
          "How does sleep environment impact skin temperature?",
          "Can stress influence skin temperature?"
        ],
        "graph_type": "avg_skin_temp_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 96
          },
          {
            "date": "2025-11-03",
            "value1": 97
          },
          {
            "date": "2025-11-10",
            "value1": 97
          },
          {
            "date": "2025-11-17",
            "value1": 96
          },
          {
            "date": "2025-11-24",
            "value1": 96
          },
          {
            "date": "2025-12-01",
            "value1": 97
          }
        ]
      },
      {
        "relevancy": 0.77,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Steady Oxygen Levels",
        "description": "Your average SpO₂ remained stable at 96%. Consistent oxygen saturation supports adequate breathing and cardiovascular health during sleep.",
        "suggestions": "Maintain a clean sleep environment and consider air quality if anything changes.",
        "related_suggested_questions": [
          "What are normal SpO₂ levels during sleep?",
          "How can sleep apnea affect oxygen saturation?",
          "Why is monitoring SpO₂ important for health?"
        ],
        "graph_type": "avg_oxy_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 94
          },
          {
            "date": "2025-11-03",
            "value1": 95
          },
          {
            "date": "2025-11-10",
            "value1": 95
          },
          {
            "date": "2025-11-17",
            "value1": 95
          },
          {
            "date": "2025-11-24",
            "value1": 94
          },
          {
            "date": "2025-12-01",
            "value1": 96
          }
        ]
      },
      {
        "relevancy": 0.63,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Respiratory Rate Decreased Slightly",
        "description": "Your average respiratory rate decreased to 16 breaths per minute from last week's 17. This slight reduction can indicate improved respiratory efficiency or relaxation.",
        "suggestions": "Continue practicing relaxation techniques, especially during wind-down routines.",
        "related_suggested_questions": [
          "What influences respiratory rate during sleep?",
          "How can exercise impact respiratory rate?",
          "What is the relationship between stress and respiratory rate?"
        ],
        "graph_type": "avg_respiration_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 17
          },
          {
            "date": "2025-11-03",
            "value1": 17
          },
          {
            "date": "2025-11-10",
            "value1": 17
          },
          {
            "date": "2025-11-17",
            "value1": 17
          },
          {
            "date": "2025-11-24",
            "value1": 16
          },
          {
            "date": "2025-12-01",
            "value1": 17
          }
        ]
      },
      {
        "relevancy": 0.8,
        "insight_type": "misc",
        "date_time": "2025-12-12 11:29:58",
        "title": "Earlier Circadian Midpoint Achieved",
        "description": "The midpoint of your sleep cycles shifted to 4:32am from 5:00am last week. Moving towards an earlier midpoint can enhance sleep consistency and quality.",
        "suggestions": "Try to maintain this earlier rhythm by avoiding late-night electronics and sticking to a regular bedtime.",
        "related_suggested_questions": [
          "What is the optimal circadian midpoint for adults?",
          "How does circadian rhythm impact energy levels?",
          "What lifestyle factors can shift circadian rhythms?"
        ],
        "graph_type": "circadian_mid_point_week",
        "graph": [
          {
            "date": "2025-10-27",
            "value1": 23334
          },
          {
            "date": "2025-11-03",
            "value1": 20571
          },
          {
            "date": "2025-11-10",
            "value1": 15999
          },
          {
            "date": "2025-11-17",
            "value1": 16305
          },
          {
            "date": "2025-11-24",
            "value1": 15666
          },
          {
            "date": "2025-12-01",
            "value1": 15291
          }
        ]
      }
    ],
    [
      {
        "relevancy": 0.9,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "REM Sleep Increased by 10% Compared to Last Month",
        "description": "Your average REM sleep increased significantly, which is essential for emotional regulation and memory consolidation. This improvement can enhance cognitive function and mood throughout the day.",
        "suggestions": "Maintain a regular sleep schedule and reduce distractions before bedtime to further enhance REM sleep.",
        "related_suggested_questions": [
          "How can improving REM sleep benefit my daytime performance?",
          "What lifestyle changes can boost my REM sleep?",
          "Does the time I go to bed affect my REM sleep duration?"
        ],
        "graph_type": "rem_sleep_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 3240
          },
          {
            "date": "2025-08-01",
            "value1": 3928
          },
          {
            "date": "2025-09-01",
            "value1": 4447
          },
          {
            "date": "2025-10-01",
            "value1": 4667
          },
          {
            "date": "2025-11-01",
            "value1": 4646
          }
        ]
      },
      {
        "relevancy": 0.85,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Deep Sleep Decreased by 5% Compared to Last Month",
        "description": "Deep sleep has decreased slightly, which might impact your physical recovery and immune function. Deep sleep is crucial for muscle repair and overall health maintenance.",
        "suggestions": "Try incorporating a wind-down routine with relaxation techniques before bed to increase deep sleep duration.",
        "related_suggested_questions": [
          "What activities can help improve my deep sleep?",
          "Does diet impact deep sleep duration?",
          "Is there a correlation between stress and reduced deep sleep?"
        ],
        "graph_type": "deep_sleep_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 5190
          },
          {
            "date": "2025-08-01",
            "value1": 4951
          },
          {
            "date": "2025-09-01",
            "value1": 4902
          },
          {
            "date": "2025-10-01",
            "value1": 5367
          },
          {
            "date": "2025-11-01",
            "value1": 4691
          }
        ]
      },
      {
        "relevancy": 0.88,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Sleep Efficiency Improved by 8% Compared to Last Month",
        "description": "Your sleep efficiency has improved, meaning you're making better use of your time in bed for actual sleep. This change can enhance your overall restfulness and energy levels.",
        "suggestions": "Continue to limit screen time before bed and maintain a calm sleep environment to keep up this positive trend.",
        "related_suggested_questions": [
          "How does sleep efficiency affect my overall health?",
          "What habits could further improve my sleep efficiency?",
          "Can diet play a role in boosting sleep efficiency?"
        ],
        "graph_type": "sleep_efficiency_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 95
          },
          {
            "date": "2025-08-01",
            "value1": 96
          },
          {
            "date": "2025-09-01",
            "value1": 95
          },
          {
            "date": "2025-10-01",
            "value1": 95
          },
          {
            "date": "2025-11-01",
            "value1": 95
          }
        ]
      },
      {
        "relevancy": 0.92,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Total Sleep Duration Increased by 12% This Month",
        "description": "Your total sleep duration has increased, supporting overall health and reducing fatigue levels. Adequate sleep duration is key for your recovery and mood.",
        "suggestions": "Keep prioritizing sleep by setting consistent bedtime and wake-up times to maintain this positive trend.",
        "related_suggested_questions": [
          "How does longer sleep enhance my daily energy levels?",
          "What are the health benefits of increased total sleep duration?",
          "How can my sleep environment support longer sleep?"
        ],
        "graph_type": "total_duration_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 25026
          },
          {
            "date": "2025-08-01",
            "value1": 26678
          },
          {
            "date": "2025-09-01",
            "value1": 25261
          },
          {
            "date": "2025-10-01",
            "value1": 26492
          },
          {
            "date": "2025-11-01",
            "value1": 26440
          }
        ]
      },
      {
        "relevancy": 0.75,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Sleep Latency Decreased by 5 Minutes Compared to Last Month",
        "description": "You're falling asleep faster, which indicates reduced stress or better relaxation techniques. Quick sleep onset often leads to a more restful night.",
        "suggestions": "Maintain relaxation habits and avoid caffeine in the evenings to continue benefiting from quicker sleep onset.",
        "related_suggested_questions": [
          "What could cause me to fall asleep faster?",
          "How does sleep latency affect sleep quality?",
          "What evening habits could improve my sleep latency?"
        ],
        "graph_type": "latency_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 7
          },
          {
            "date": "2025-08-01",
            "value1": 6
          },
          {
            "date": "2025-09-01",
            "value1": 9
          },
          {
            "date": "2025-10-01",
            "value1": 6
          },
          {
            "date": "2025-11-01",
            "value1": 6
          }
        ]
      },
      {
        "relevancy": 0.87,
        "insight_type": "sleep",
        "date_time": "2025-12-12 11:29:58",
        "title": "Restfulness Improved by 10% Compared to Last Month",
        "description": "You're experiencing more restful nights, likely due to improvements in sleep quality and duration. This positively impacts your daily cognitive and physical performance.",
        "suggestions": "Continue with your current pre-sleep routine and keep stress-minimizing practices in order.",
        "related_suggested_questions": [
          "How does restfulness affect my daytime energy?",
          "What factors contribute to a restful sleep?",
          "Can meditation improve my restfulness?"
        ],
        "graph_type": "restfullness_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 3
          },
          {
            "date": "2025-08-01",
            "value1": 3
          },
          {
            "date": "2025-09-01",
            "value1": 4
          },
          {
            "date": "2025-10-01",
            "value1": 4
          },
          {
            "date": "2025-11-01",
            "value1": 4
          }
        ]
      },
      {
        "relevancy": 0.84,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "HRV Decreased by 15% This Month",
        "description": "A decrease in HRV may suggest increased stress or insufficient recovery. Lower HRV can indicate that your body is under more strain than usual.",
        "suggestions": "Incorporate mindfulness practices and ensure adequate rest days to improve your HRV.",
        "related_suggested_questions": [
          "How does HRV reflect my stress levels?",
          "What strategies can I use to raise my HRV?",
          "Does exercise frequency affect HRV?"
        ],
        "graph_type": "hrv_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 34
          },
          {
            "date": "2025-08-01",
            "value1": 34
          },
          {
            "date": "2025-09-01",
            "value1": 39
          },
          {
            "date": "2025-10-01",
            "value1": 44
          },
          {
            "date": "2025-11-01",
            "value1": 31
          }
        ]
      },
      {
        "relevancy": 0.86,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Resting Heart Rate Increased by 8% This Month",
        "description": "An increase in resting heart rate can signal stress, overwork, or lack of recovery. Monitoring this can help you identify potential health or fitness issues.",
        "suggestions": "Prioritize stress management techniques and ensure sufficient recovery in your exercise regimen.",
        "related_suggested_questions": [
          "How can reducing stress lower my resting heart rate?",
          "What lifestyle changes can I make to improve my resting heart rate?",
          "Does better diet affect my resting heart rate?"
        ],
        "graph_type": "rhr_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 79
          },
          {
            "date": "2025-08-01",
            "value1": 78
          },
          {
            "date": "2025-09-01",
            "value1": 79
          },
          {
            "date": "2025-10-01",
            "value1": 74
          },
          {
            "date": "2025-11-01",
            "value1": 80
          }
        ]
      },
      {
        "relevancy": 0.75,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Average Skin Temperature Decreased by 0.5% This Month",
        "description": "A decrease in skin temperature might indicate effective thermoregulation, especially during sleep, enhancing rest and recovery.",
        "suggestions": "Maintain a cool, comfortable sleeping environment to continue supporting healthy sleep patterns.",
        "related_suggested_questions": [
          "How does skin temperature affect my sleep quality?",
          "What can I do to regulate my skin temperature during sleep?",
          "Can my diet influence skin temperature?"
        ],
        "graph_type": "avg_skin_temp_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 97
          },
          {
            "date": "2025-08-01",
            "value1": 96
          },
          {
            "date": "2025-09-01",
            "value1": 96
          },
          {
            "date": "2025-10-01",
            "value1": 96
          },
          {
            "date": "2025-11-01",
            "value1": 96
          }
        ]
      },
      {
        "relevancy": 0.7,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Average SpO₂ Increased by 2% Compared to Last Month",
        "description": "An increase in oxygen saturation levels suggests improved respiratory health and possibly better sleep quality, as your body is receiving adequate oxygen.",
        "suggestions": "Continue with regular exercise and lung-strengthening activities to maintain optimal SpO₂ levels.",
        "related_suggested_questions": [
          "What activities can help boost my SpO₂ levels?",
          "Does my breathing technique during the day affect SpO₂?",
          "Can sleep position impact my SpO₂ readings?"
        ],
        "graph_type": "avg_oxy_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 95
          },
          {
            "date": "2025-08-01",
            "value1": 95
          },
          {
            "date": "2025-09-01",
            "value1": 95
          },
          {
            "date": "2025-10-01",
            "value1": 95
          },
          {
            "date": "2025-11-01",
            "value1": 95
          }
        ]
      },
      {
        "relevancy": 0.79,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Average Respiratory Rate Decreased by 1 BPM This Month",
        "description": "A lower respiratory rate can indicate improved cardiovascular fitness and more efficient breathing during sleep, enhancing overall rest.",
        "suggestions": "Maintain regular aerobic exercise to continue supporting efficient respiration rates.",
        "related_suggested_questions": [
          "How does cardiovascular fitness impact my respiratory rate?",
          "What breathing exercises can improve my sleep?",
          "Can changes in my diet affect respiratory rate?"
        ],
        "graph_type": "avg_respiration_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 17
          },
          {
            "date": "2025-08-01",
            "value1": 17
          },
          {
            "date": "2025-09-01",
            "value1": 17
          },
          {
            "date": "2025-10-01",
            "value1": 17
          },
          {
            "date": "2025-11-01",
            "value1": 17
          }
        ]
      },
      {
        "relevancy": 0.83,
        "insight_type": "readiness",
        "date_time": "2025-12-12 11:29:58",
        "title": "Circadian Midpoint Shifted Earlier by 20 Minutes",
        "description": "Your circadian rhythm is adjusting, potentially leading to better alignment with natural sleep-wake cycles. This can enhance sleep quality and daytime alertness.",
        "suggestions": "Try to wake up and see natural light soon after to reinforce this healthier sleep pattern.",
        "related_suggested_questions": [
          "What benefits come from an earlier circadian midpoint?",
          "How does my evening routine affect my circadian rhythm?",
          "What are the signs of a healthy circadian rhythm?"
        ],
        "graph_type": "circadian_mid_point_month",
        "graph": [
          {
            "date": "2025-06-01",
            "value1": null
          },
          {
            "date": "2025-07-01",
            "value1": 12894
          },
          {
            "date": "2025-08-01",
            "value1": 18415
          },
          {
            "date": "2025-09-01",
            "value1": 14380
          },
          {
            "date": "2025-10-01",
            "value1": 18409
          },
          {
            "date": "2025-11-01",
            "value1": 17157
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
        _cards.value = dummy*/

        //--
        viewModelScope.launch {
            userRepository.getInsightLvl1List().collect { resource ->
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
                        resource.data?.data.let {
                            if (it.isNullOrEmpty()) {
                                _cards.value = ArrayList()
                                return@let
                            }

                            val response = ArrayList<InsightItemResponseModel>()
                            it.forEach { insightsList ->
                                response.addAll(insightsList)
                            }
                            val dummy = generateData(response)
                            _cards.value = dummy
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
                        // REM_SLEEP (double_bar_plot with Deep Sleep)
                    }

                    GraphType.Day.DEEP_SLEEP,
                    GraphType.Week.DEEP_SLEEP,
                    GraphType.Month.DEEP_SLEEP -> {
                        graphDataConvertor.generateSleepMultiBarChartData(
                            it, SleepInternalLaunchState.DEEP_SLEEP
                        )?.let { data ->
                            list.add(data)
                        }
                        // DEEP_SLEEP (double_bar_plot with REM Sleep)
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
                        // RESPIRATION (line_plot)
                    }

                    GraphType.Day.CIRCADIAN_MID_POINT,
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
//
//                    "hr" -> {
//                        list.add(graphDataConvertor.generateHrGraphData(it))
//                    }
//
//                    "stress" -> {
//                        list.add(graphDataConvertor.generateStressGraphData(it))
//                    }
//
//                    "daytime" -> {
//                        list.add(graphDataConvertor.generateDayTimeGraphData(it))
//                    }
//
//                    "sleep_stage" -> {
//                        list.add(graphDataConvertor.generateSleepBreakupGraphData(it))
//                    }
//                    "sleep_movement" -> {
//                        list.add(graphDataConvertor.generateSleepMovementData(it))
//                    }
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

    private fun getPeriod(data: InsightItemResponseModel) = when {
        data.graph_type?.endsWith("week") == true -> InternalSelectedPeriod.WEEK
        data.graph_type?.endsWith("month") == true -> InternalSelectedPeriod.MONTH
        else -> InternalSelectedPeriod.DAY
    }

    private fun getContributor(data: InsightItemResponseModel): SleepInternalLaunchState {
        return when {
            data.graph_type == null -> SleepInternalLaunchState.DEEP_SLEEP
            data.graph_type.startsWith("circadian_mid_point_day") -> SleepInternalLaunchState.DEEP_SLEEP
            data.graph_type.startsWith("hour_vs_need_day") -> SleepInternalLaunchState.HOUR_VS_NEED
            data.graph_type.startsWith("restorative_sleep") -> SleepInternalLaunchState.RESTORATIVE_SLEEP
            data.graph_type.startsWith("rem_sleep") -> SleepInternalLaunchState.REM_SLEEP
            data.graph_type.startsWith("deep_sleep") -> SleepInternalLaunchState.DEEP_SLEEP
            data.graph_type.startsWith("sleep_perf") -> SleepInternalLaunchState.SLEEP_PERFORMANCE
            data.graph_type.startsWith("avg_respiration") -> SleepInternalLaunchState.RESPIRATORY_RATE
            else -> SleepInternalLaunchState.DEEP_SLEEP
        }
    }
}
