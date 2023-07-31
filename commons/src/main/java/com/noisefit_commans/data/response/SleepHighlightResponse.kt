package com.noisefit_commans.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class SleepHighlightResponse(
    @SerializedName("breakup_duration") val breakup_duration: List<SleepBreakup>? = null,
    @SerializedName("bedtime_variance") val bedtime_variance: BedTimeVariance?,
    @SerializedName("total_duration") val total_duration: Int? = 0,
    @SerializedName("avg_duration") val avg_duration: Int? = 0,
    @SerializedName("sleep_breakup") val sleep_breakup: List<SleepBreakup>? = null
)

data class BedTimeVariance(
    val avg: Double? = 0.0,
    val available: Boolean,
    val breakup: List<BedTimeVarianceBreakup>? = null
)

data class BedTimeVarianceBreakup(
    val date: String? = null,
    val duration_diff: Int? = 0
)


@Parcelize
data class SleepBreakup(
    @SerializedName("total_deep") val deep: Int,
    @SerializedName("total_light") val light: Int,
    @SerializedName("total_sober") val sober: Int,
    @SerializedName("hour_of_the_day") val hour_of_the_day: Int,
    @SerializedName("month") val month: Int,
    @SerializedName("date") val date: String? = null,
    @SerializedName("total_awake") val awake: Int,
    @SerializedName("hourly_breakup") val hourly_breakup: List<SleepHourBreakup>? = null,
    @SerializedName("duration") val duration: Int,
    @SerializedName("avg_duration") val avg_duration: Int,
    @SerializedName("rem_count") val rem: Int,
    @SerializedName("sleep_score") val sleepScore: Int? = 0,
    @SerializedName("breath_quality") val breathQuality: Int
) : Parcelable


@Parcelize
data class SleepHourBreakup(
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("sleep_type") val type: String,
    @SerializedName("hour_of_the_day") val hour_of_the_day: Int,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("duration") val duration: Int
) : Parcelable


data class SleepCumulative(
    @SerializedName("total_deep") val deep: Int,
    @SerializedName("total_light") val light: Int,
    @SerializedName("total_sober") val sober: Int,
    @SerializedName("total_awake") val awake: Int,
    @SerializedName("total_duration") val totalDuration: Int,
    @SerializedName("rem_count") val rem: Int,
    @SerializedName("sleep_score") val sleepScore: Int,
    @SerializedName("breath_quality") val breathQuality: Int,
    @SerializedName("available_sleep_types") val availableSleepTypes: String
)