package com.oreo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.oreo.ui.DataType
import kotlinx.parcelize.Parcelize

data class TestDataModel(
    @SerializedName("today_graph_data")
    val todayGraphData: List<GraphData>? = null,
    @SerializedName("readiness_score")
    val readinessScore: ScoreData? = null,
    @SerializedName("readiness_contribution")
    val readinessContribution: List<Contribution>? = null,
    @SerializedName("heart_rate")
    val heartRate: HearRate? = null,
    @SerializedName("heart_rate_variability")
    val heartRateVariability: HearRateVariability? = null
)

@Parcelize
data class TestUserData(
    val type: DataType,
    val time: String? = null,
    val data: String
) : Parcelable

data class GraphData(val data: String)
data class ScoreData(
    @SerializedName("score")
    val score: Int?=null,
    @SerializedName("quality")
    val quality: String?=null,
    @SerializedName("rest_hr")
    val restHr: Int?=null,
    @SerializedName("rest_hr_unit")
    val restHrUnit: String?=null,
    @SerializedName("hr_variability")
    val hrVariability: Int?=null,
    @SerializedName("hr_variability_unit")
    val hrVariabilityUnit: String?=null,
    @SerializedName("temperature")
    val temperature: Int?=null,
    @SerializedName("temperature_unit")
    val temperatureUnit: String?=null,
    @SerializedName("respiratory_rate")
    val respiratoryRate: Int?=null,
    @SerializedName("respiratory_unit")
    val respiratoryUnit: String?=null,
    @SerializedName("total_sleep")
    val totalSleep: Int?=null,
    @SerializedName("time_in_bed")
    val timeInBed: Int?=null,
    @SerializedName("sleep_efficiency")
    val sleepEfficiency: Int?=null,
    @SerializedName("goal_progress")
    val goalProgress: Int?=null,
    @SerializedName("goal_progress_unit")
    val goalProgressUnit: String?=null,
    @SerializedName("total_burn")
    val totalBurn: Int?=null,
    @SerializedName("total_burn_unit")
    val totalBurnUnit: String?=null,
    @SerializedName("steps_count")
    val stepsCount: Int?=null,
    val distance: Int?=null,
)

data class Contribution(val title: String, val progress: Int, val remarks: String)
data class HearRate(
    @SerializedName("lowest_heart_rate")
    val lowestHeartRate: Int?=null,
    @SerializedName("unit")
    val unit: String?=null,
    @SerializedName("average")
    val average: Int?=null,
    @SerializedName("heart_rate_graph_data")
    val heartRateGraphData: List<GraphData>? = null
)
data class HearRateVariability(
    @SerializedName("average_hrv")
    val averageHrv: Int?=null,
    @SerializedName("unit")
    val unit: String?=null,
    @SerializedName("max")
    val max: Int?=null,
    @SerializedName("heart_rate_graph_data")
    val heartRateGraphData: List<GraphData>? = null
)
