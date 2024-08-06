package com.oreo.data.model.health

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class OreoSleepModel(
    var date: String,//YYYY-MM-DD
    val sleeps: List<MultiSleepModel>? = null,
    val avg_hrv:Int?=null,

    var sleep_score: CommonDataModel? = null,
    var totalSleep: CommonDataModel? = null,
    var timeInBed: CommonDataModel? = null,
    var sleepEfficiency: CommonDataModel? = null,
    var restFullness: CommonDataModel? = null,
    var restingHr: CommonDataModel? = null,
    var remSleep: CommonDataModel? = null,
    var lightSleep: CommonDataModel? = null,
    var awake: CommonDataModel? = null,
    var deepSleep: CommonDataModel? = null,
    var latency: CommonDataModel? = null,
    var timing: CommonDataModel? = null,
    var hr: CommonListDataModel? = null,
    var hrv: CommonListDataModel? = null,
    var oxy: CommonListDataModel? = null,
    val naps: List<Nap>? = null,
    @SerializedName("int_nudges") val nudges: List<Nudges>?,
    var hourly_breakup: List<SleepHourlyBreakup>? = null,
    var night_time_movement: List<SleepMovementBreakup>? = null,
    @SerializedName("sleep_nap_score_impact") val sleepNapScoreImpact: Int? = null,
    @SerializedName("no_of_naps") val noOfNaps: Int? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null
)

data class MultiSleepModel(
    @SerializedName("total_duration") val totalDuration: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("sleep_score") val sleepScore: Int? = null,
    @SerializedName("avg_hrv") val avgHrv: Int? = null,
    var hourly_breakup: List<SleepHourlyBreakup>? = null,
    )

data class Nap(
    val date: String,
    val id: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("sleep_score_impact") val sleepScoreImpact: Int? = null,
    @SerializedName("after_7pm") val after7pm: String,
    @SerializedName("readiness_score_impact") val readinessScoreImpact: Int? = null,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("msg") val msg: String? = null,
    val duration: Int? = null,
    @SerializedName("is_next_day_nap") val isNextDayNap: Boolean = false,
    @SerializedName("is_day_nap") val isDayNap: Boolean = false
)

@Parcelize
data class Nudges(
    val label: String, val message: String
) : Parcelable

data class SleepHourlyBreakup(
    val date: String,//023-05-15
    val duration: Int,
    val sleep_type: String,
    val start_time: String,
    val end_time: String,
)

data class SleepMovementBreakup(
    val date: String,//023-05-15
    val duration: Int,
    val movement_type: String,
    val start_time: String,
    val end_time: String,
)

data class CommonListDataModel(
    val value: List<Int>,
    val avg: Int? = null,
    val max: Int? = null,
    val low: Int? = null,
)

data class CommonDataModel(
    val value: Int?,
    val valPrcnt: Int?,
    val value_percentage: Int?,//for deep and rem sleep in stage analysis
    val text: String,
    val status: String,//"warning/good"
)

data class CommonDataModelString(
    val value: String?,
    val text: String,
    val status: String,//"warning/good"
)
