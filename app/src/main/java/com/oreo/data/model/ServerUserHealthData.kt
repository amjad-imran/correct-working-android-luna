package com.oreo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.ColorfitData
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.data.model.health.WelcomeData
import kotlinx.parcelize.Parcelize


data class ServerUserHealthResponse(
    val data: List<ServerUserHealthData>,
    val trends: TrendsData? = null,
    @SerializedName("register_date")
    val registerDate: Int? = null,

    @SerializedName("first_stress")
    val firstStress: String? = null,//2024-02-07

    @SerializedName("stress_beta")
    val stressBeta: Boolean? = null,
    @SerializedName("enable_ai_2")
    val enableAi: Boolean? = null,

    @SerializedName("temp_base")
    val tempBaseLine: Float? = null,
) : ColorfitData()

data class TrendsData(
    @SerializedName("activitityScoreAvg")
    val activityScoreAvg: ODashboardActivityScoreModel? = null,
    @SerializedName("readinessScoreAvg")
    val readinessScoreAvg: ODashboardReadinessScoreModel? = null,
    @SerializedName("sleepScoreAvg")
    val sleepScoreAvg: ODashboardSleepScoreModel? = null,
    val welcome: WelcomeData? = null
)

data class ServerUserHealthData(
    val date: String,//YYYY-MM-dd
    var sleep: OreoSleepModel? = null,
    var activity: OreoActivityModel? = null,
    var readiness: OreoReadinessModel? = null,
    var heart: DataBreakup? = null,
    var stress: Stress? = null
)

data class Stress(
    @SerializedName("break_up")
    val breakUp: List<Int>,
    @SerializedName("stress_value")
    val stressValue: StressValue? = null,
    val nudges: List<StressNudge>? = null,

    @SerializedName("typical_calm")
    val typicalCalm: Int? = null,
    @SerializedName("typical_focused")
    val typicalFocused: Int? = null,
    @SerializedName("typical_stressed")
    val typicalStressed: Int? = null,
    @SerializedName("non_active")
    val nonActive: NonActive? = null

)

data class StressValue(
    val value: Int? = null,
    val text: String? = null,
    @SerializedName("last_updated")
    val lastUpdated: Long? = null
)

@Parcelize
data class StressNudge(
    val label: String,
    val value: String? = null,
    val message: String
) : Parcelable

data class DataBreakup(
    val break_up: List<Int>
)

data class NonActive(
    @SerializedName("non_active_calm")
    val nonActiveCalm: Int,
    @SerializedName("non_active_focused")
    val nonActiveFocused: Int,
    @SerializedName("non_active_stressed")
    val nonActiveStressed: Int,
    @SerializedName("typical_non_active_calm")
    val typicalNonActiveCalm: Int? = null,
    @SerializedName("typical_non_active_focused")
    val typicalNonActiveFocused: Int? = null,
    @SerializedName("typical_non_active_stressed")
    val typicalNonActivestressed: Int? = null
)
