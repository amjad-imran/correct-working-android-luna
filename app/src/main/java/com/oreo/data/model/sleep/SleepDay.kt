package com.oreo.data.model.sleep

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.oreo.data.model.health.CommonDataModel
import com.oreo.data.model.health.CommonDataModelString
import com.oreo.data.model.health.Nap
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.model.health.SleepMovementBreakup
import kotlinx.parcelize.Parcelize

data class SleepDay(
    val date: String,

    @SerializedName("sleep_score")
    var sleepScore: CommonDataModel? = null,

    @SerializedName("sleep_duration")
    var sleepDuration: CommonDataModel? = null,

    @SerializedName("sleep_need")
    var sleepNeed: Int? = null,

    @SerializedName("time_in_bed")
    var timeInBed: Int? = null,

    @SerializedName("rem_sleep")
    var remSleep: CommonDataModel? = null,

    @SerializedName("deep_sleep")
    var deepSleep: CommonDataModel? = null,

    @SerializedName("master_sleep_start")
    var masterSleepStart: String? = null,

    @SerializedName("master_sleep_end")
    var masterSleepEnd: String? = null,

    var latency: CommonDataModel? = null,


    var efficiency: CommonDataModel? = null,

    @SerializedName("sleep_performance")
    var sleepPerformance: Int? = null,

    @SerializedName("prev_14_day_bed")
    var prev14DayBed: Int? = null,

    @SerializedName("prev_14_day_awake")
    var prev14DayAwake: Int? = null,

    var restfulness: CommonDataModel? = null,

    var timing: CommonDataModelString? = null,
    @SerializedName("health_trend")
    var healthTrend: HealthTrend? = null,
    @SerializedName("sleep_child")
    val sleepChild: List<MultiSleep>? = null,

    val naps: List<Nap>? = null,
    val nudges: List<Nudges>?,


    )

data class SleepSummary(
    val rem: SleepSummaryValue,
    val deep: SleepSummaryValue,
    val awake: SleepSummaryValue,
    val light: SleepSummaryValue,
)

data class SleepSummaryValue(
    val curr_val: Int? = null,
    val avg: Int? = null,
)

@Parcelize
data class HealthTrend(
    val resp: HealthTrendValue? = null,
    val rhr: HealthTrendValue? = null,
    val hrv: HealthTrendValue? = null,
    @SerializedName("skin_temp")
    val skinTemp: HealthTrendValue? = null,
    val bloodOxy: HealthTrendValue? = null,
) : Parcelable


@Parcelize
data class HealthTrendValue(
    val value: Double? = null,
    val text: String? = null,
    val status: String? = null
) : Parcelable

data class MultiSleep(
    val start_time: String? = null,
    val end_time: String? = null,
    val sleep_impact: Int? = null,
    val sleep_score: Int? = null,
    var hourly: List<SleepHourlyBreakup>? = null,
    var night_time_movement: List<SleepMovementBreakup>? = null,
    var summary: SleepSummary? = null
)
