package com.oreo.data.model.sleep

import com.google.gson.annotations.SerializedName
import com.oreo.data.model.health.CommonDataModel
import com.oreo.data.model.health.CommonDataModelString
import com.oreo.data.model.health.Nap
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.model.health.SleepMovementBreakup

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

    var summary: SleepSummary? = null,

    var efficiency: CommonDataModel? = null,

    @SerializedName("sleep_performance")
    var sleepPerformance: Int? = null,

    @SerializedName("prev_14_day_bed")
    var prev14DayBed: Int? = null,

    @SerializedName("prev_14_day_awake")
    var prev14DayAwake: Int? = null,

    var restfulness: CommonDataModel? = null,

    var timing: CommonDataModelString? = null,


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

data class MultiSleep(
    val start_time: String? = null,
    val end_time: String? = null,
    var hourly: List<SleepHourlyBreakup>? = null,
    var night_time_movement: List<SleepMovementBreakup>? = null,
)
