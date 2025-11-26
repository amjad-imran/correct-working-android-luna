package com.oreo.ui.lifeos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import com.oreo.ui.lifeos.charts.InsightCardUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LifeOsDashViewModel @Inject constructor(
    val localDataStore: DataStoredInterface,
) : ViewModel() {

    private val _questions = MutableLiveData<List<String>>()
    val questions: LiveData<List<String>> get() = _questions

    private val _whatsNew = MutableLiveData<List<String>>()
    val whatsNew: LiveData<List<String>> get() = _whatsNew

    val destinationData = MutableLiveData<LifeOsDestinations?>()

    private val _insightsCardsData = MutableLiveData<List<InsightCardUiModel>>()
    val insightsCardsData: LiveData<List<InsightCardUiModel>> get() = _insightsCardsData

    init {
        loadSuggestedQuestions()
        loadWhatsNew()
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

    fun loadSuggestedQuestions() {
        _questions.value = listOf(
            "Teach me about my sleep score",
            "Create a diet plan for me",
            "Create a workout plan for me"
        )
    }

    fun loadInsightsData() {
        viewModelScope.launch {
            val jsonRes = """
            [
                {
                    "relevancy": 0.95,
                    "title": "Total sleep duration increased 42% compared to the previous period",
                    "description": "Your average total sleep went from 3h 44m to 5h 19m (a 42% increase). That’s a meaningful gain in time in bed — likely giving your body more opportunity for recovery and memory consolidation. While 5h 19m is an improvement, it's still under typical adult recommendations, so there may be room to keep building consistency.",
                    "suggestions": "Try keeping a consistent bedtime and wake time for the next week; aim to gradually add 20–30 minutes to your sleep window until you reach your target. Limit late caffeine and wind down with low-light, low-screen activities 30–60 minutes before bed.",
                    "related_suggested_questions": [
                        "Did you change your bedtime or wake time the night(s) you slept longer?",
                        "Were there differences in caffeine, alcohol, or evening activity on nights you slept more?",
                        "How did your morning mood, focus, or energy compare after the longer sleep?"
                    ],
                    "graph_type": "total_duration",
                    "graph": {
                        "2025-11-25": 19110,
                        "2025-11-18": 12540,
                        "2025-11-19": 14340
                    }
                },
                {
                    "relevancy": 0.85,
                    "title": "Circadian midpoint shifted later by ~7h 16m",
                    "description": "Your sleep midpoint moved from around 22:07 on earlier nights to 05:23 on Nov 25 — a shift of roughly 7h 16m. That large change suggests your sleep timing moved much later (or your most recent night was shifted across the typical night boundary), which can affect daytime alertness and make it harder to maintain regular rhythms.",
                    "suggestions": "If you want to bring your rhythm earlier, get bright light exposure in the morning and dim lights in the evening. Keep wake time consistent and avoid late-night bright screens or heavy meals.",
                    "related_suggested_questions": [
                        "Did you go to bed much later or sleep in on Nov 25 compared to other days?",
                        "Was there evening light exposure, caffeine, or social activity that might explain the later midpoint?",
                        "Do you notice feeling more jet-lagged or groggy after nights with later midpoints?"
                    ],
                    "graph_type": "circadian_mid_point",
                    "graph": {
                        "2025-11-25": "05:23:30",
                        "2025-11-18": "22:14:30",
                        "2025-11-19": "21:59:30"
                    }
                },
                {
                    "relevancy": 0.8,
                    "title": "Daily stress levels dropped ~25% on Nov 25 versus earlier last week",
                    "description": "Your stress score fell from an average of 64 in prior days to 48 on Nov 25 (about a 25% drop). That lines up with more calm minutes recorded that day and could reflect lower perceived pressure, better rest, or effective coping strategies — though day-to-day context matters.",
                    "suggestions": "Keep the practices that coincided with this calmer day (short breaks, breathing exercises, an earlier wind-down). Consider tracking what changed (workload, exercise, social time) so you can repeat helpful habits.",
                    "related_suggested_questions": [
                        "Did you change your schedule, workload, or exercise on Nov 25?",
                        "Was the longer sleep the night before associated with the lower stress that day?",
                        "Which calming activities (breathing, walks, less screen time) were present on your lower-stress day?"
                    ],
                    "graph_type": "hrv",
                    "graph": {
                        "2025-11-25": 70,
                        "2025-11-18": null,
                        "2025-11-19": null
                    }
                }
            ]
        """.trimIndent()

            val type = object : TypeToken<List<InsightItemResponseModel>>() {}.type
            val rawListData = Gson().fromJson<List<InsightItemResponseModel>>(jsonRes, type)

            if(rawListData.isNullOrEmpty()){
                _insightsCardsData.value = ArrayList()
                return@launch
            }

            processInsightsData(rawListData)
        }
    }

    private fun processInsightsData(rawListData: List<InsightItemResponseModel>){

    }

    fun loadWhatsNew() {
        _whatsNew.value = listOf(
            "New Timeline: streamlined logging for Supplements and Recovery",
            "AI coaching improvements: better context understanding and tips",
            "Dashboard tweaks: faster loading and refreshed visuals"
        )
    }

    enum class LifeOsDestinations{
        BEGIN_FRAG, QUES_FRAG, LIFE_OS_MAIN
    }
}
