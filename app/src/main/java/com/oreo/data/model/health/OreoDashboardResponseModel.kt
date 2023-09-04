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
    val readinessScore: Int? = 0,
    val status: String? = "",
    val nudges: List<String>? = null,
)

data class ODashboardActivityScoreModel(
    val activityScore: Int? = 0,
    val trend: Int? = 0,
    val value: List<Int>? = ArrayList(),
)

data class ODashboardReadinessScoreModel(
    val readinessScore: Int? = 0,
    val trend: Int? = 0,
    val value: List<Int>? = ArrayList(),
)

data class ODashboardSleepScoreModel(
    val sleepScore: Int? = 0,
    val trend: Int? = 0,
    val value: List<Int>? = ArrayList(),
)

data class ODashboardActivityModel(
    val activityScore: Int? = 0,
    val activeCalories: Int? = 0,
    val status: String? = "",
    val nudges: List<String>? = null,
)

data class ODashboardSleepModel(
    val sleepScore: Int? = 0,
    val totalSleep: Int? = 0,
    val lowestHr: Int? = 0,
    val sleepStage: ArrayList<ODashboardSleepStageModel> = ArrayList(),
    val status: String? = "",
    @SerializedName("start_time")
    val startTime: String? = "",
    @SerializedName("end_time")
    val endTime: String? = "",
)

data class ODashboardSleepStageModel(
    val duration: Int? = 0,
    @SerializedName("start_time")
    val startTime: String? = "",
    @SerializedName("end_time")
    val endTime: String? = "",
    @SerializedName("sleep_type")
    val sleepType: String? = "",
)