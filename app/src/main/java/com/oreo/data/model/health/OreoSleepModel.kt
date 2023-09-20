package com.oreo.data.model.health

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class OreoSleepModel(
    var date: String,//YYYY-MM-DD
    var sleepScore: CommonDataModel? = null,
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
    val nudges: List<Nudges>?,
    var hourly_breakup: List<SleepHourlyBreakup>? = null,
    var night_time_movement: List<SleepMovementBreakup>? = null,
)

@Parcelize
data class Nudges(
    val label: String,
    val message: String
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
