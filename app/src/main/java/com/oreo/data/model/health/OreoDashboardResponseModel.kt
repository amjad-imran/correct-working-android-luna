package com.oreo.data.model.health

import com.google.gson.annotations.SerializedName

data class OreoDashboardResponseModel(
    @SerializedName("sleep")
    val sleep: ODashboardSleepModel? = null,
    @SerializedName("readiness")
    val readiness: ODashboardReadinessModel? = null,
    @SerializedName("activity")
    val activity: ODashboardActivityModel? = null
    )

data class InfoTextData(val title: String, val content: String)
data class InfoVideoData(val url: String, val time: String, val title: String)
data class WelcomeData(
    val care: InfoTextData,
    val welcome: InfoTextData,
    val readiness_media: InfoVideoData,
    val sleep_media: InfoVideoData,
    val activity_media: InfoVideoData,
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
    val inactiveMinutes: Int? = null,
    val status: String? = "",
    val nudges: List<String>? = null,
)

data class ODashboardSleepModel(
    val sleepScore: Int? = null,
    val totalSleep: Int? = null,
    @SerializedName("resting_hr")
    val restingHr: Int? = null,
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