package com.noisefit_commans.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class SleepHistoryResponse(

    @SerializedName("history_type") val history_type: String? = null,
    @SerializedName("avg_duration") val avg_duration: Int? = null,
    @SerializedName("cumulative") val cumulative: SleepCumulative? = null,
    @SerializedName("total_light") val total_light: Int? = null,
    @SerializedName("total_deep") val total_deep: Int? = null,
    @SerializedName("total_sober") val total_sober: Int? = null,
    @SerializedName("total_awake") val total_awake: Int? = null,
    @SerializedName("hr_max") val hr_max: Int? = 0,
    @SerializedName("hr_min") val hr_min: Int? = 0,
    @SerializedName("stress_max") val stress_max: Int? = 0,
    @SerializedName("stress_min") val stress_min: Int? = 0,
    @SerializedName("avg_bedtime") val avg_bedtime: String? = null,
    @SerializedName("breakup_duration") val sleepHistory: ArrayList<SleepBreakup>? = null
)

@Parcelize
data class SleepBreakup(
    @SerializedName("total_deep") val deep: Int? = 0,
    @SerializedName("total_light") val light: Int? = 0,
    @SerializedName("total_sober") val sober: Int? = 0,
    @SerializedName("hour_of_the_day") val hour_of_the_day: Int,
    @SerializedName("month") val month: Int,
    @SerializedName("date") val date: String? = null,
    @SerializedName("total_awake") val awake: Int? = 0,
    @SerializedName("hourly_breakup") val hourly_breakup: List<SleepHourBreakup>? = null,
    @SerializedName("hr_breakup") val hr_breakup: List<SleepHeartRate>? = null,
    @SerializedName("stress_breakup") val stress_breakup: List<SleepHeartRate>? = null,
    @SerializedName("hr_max") val hr_max: Int? = 0,
    @SerializedName("hr_min") val hr_min: Int? = 0,
    @SerializedName("stress_max") val stress_max: Int? = 0,
    @SerializedName("stress_min") val stress_min: Int? = 0,
    @SerializedName("duration") val duration: Int,
    @SerializedName("avg_duration") val avg_duration: Int,
    @SerializedName("rem_count") val rem: Int,
    @SerializedName("total_duration") val total_duration: Int,
    @SerializedName("start_time") val start_time: String? = null,
    @SerializedName("end_time") val end_time: String? = null,
    @SerializedName("sleep_score") var sleepScore: Int? = 0,
    @SerializedName("breath_quality") val breathQuality: Int
) : Parcelable

@Parcelize
data class SleepHeartRate(
    val avg_value: Int? = 0,
    val value: Int? = 0,
    val date: String? = null,
    val time: String? = null,
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