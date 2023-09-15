package com.oreo.data.model.health

import com.google.gson.annotations.SerializedName

data class OreoDashboardResponseModel(
    @SerializedName("sleep")
    val sleep: ODashboardSleepModel? = null,
    @SerializedName("readiness")
    val readiness: ODashboardReadinessModel? = null,
    @SerializedName("activity")
    val activity: ODashboardActivityModel? = null,
    @SerializedName("activitityScoreAvg")
    val activityScoreAvg: ODashboardActivityScoreModel? = null,
    @SerializedName("sleepScoreAvg")
    val sleepScoreAvg: ODashboardSleepScoreModel? = null,
    @SerializedName("readinessScoreAvg")
    val readinessScoreAvg: ODashboardReadinessScoreModel? = null,
    @SerializedName("registerDate")
    val registerDate: Int? = null,

    )

data class ODashboardReadinessModel(
    val readinessScore: Int? = null,
    val status: String? = "",
    val nudges: List<String>? = null,
)

data class ODashboardActivityScoreModel(
    val activityScore: Int? = null,
    val trend: Int? = null,
    val value: List<Int>? = ArrayList(),
)

data class ODashboardReadinessScoreModel(
    val readinessScore: Int? = null,
    val trend: Int? = null,
    val value: List<Int>? = ArrayList(),
)

data class ODashboardSleepScoreModel(
    val sleepScore: Int? = null,
    val trend: Int? = null,
    val value: List<Int>? = ArrayList(),
)

data class ODashboardActivityModel(
    val activityScore: Int? = null,
    val activeCalories: Int? = null,
    val status: String? = "",
    val nudges: List<String>? = null,
)

data class ODashboardSleepModel(
    val sleepScore: Int? = null,
    val totalSleep: Int? = null,
    val lowestHr: Int? = null,
    val sleepStage: ArrayList<ODashboardSleepStageModel> = ArrayList(),
    val status: String? = "",
    @SerializedName("start_time")
    val startTime: String? = "",
    @SerializedName("end_time")
    val endTime: String? = "",
)

data class ODashboardSleepStageModel(
    val duration: Int? = null,
    @SerializedName("start_time")
    val startTime: String? = "",
    @SerializedName("end_time")
    val endTime: String? = "",
    @SerializedName("sleep_type")
    val sleepType: String? = "",
)