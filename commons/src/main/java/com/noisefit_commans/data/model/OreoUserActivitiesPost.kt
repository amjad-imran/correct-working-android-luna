package com.noisefit_commans.data.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

open class OreoUserActivitiesPost {
    open fun serialize(): String = Gson().toJson(this)
}

data class OreoUserDataPost(
    @SerializedName("sleeps") var sleeps: List<OreoSleepNetworkEntity>? = null,
    @SerializedName("activities") var activities: List<OreoStepsNetworkEntity>? = null,
    @SerializedName("oxygen") var bloodOxygen: List<OreoCommonNetworkEntity>? = null,
    @SerializedName("hrv") var stress: List<OreoCommonNetworkEntity>? = null,
    @SerializedName("heart_rate") var heartRateHistory: List<OreoHeartNetworkEntity>? = null,
    @SerializedName("temperature") var bodyTemperature: List<OreoBodyTempNetworkEntity>? = null,
    @SerializedName("respiration") var respiratory: List<OreoCommonNetworkEntity>? = null
) : OreoUserActivitiesPost()


data class OreoStepsNetworkEntity(
    @SerializedName("hourly_breakup") var hourlyBreakup: List<HourlyBreakup>? = null,
    @SerializedName("day_break_up") var dayBreakup: DayBreakup? = null,
    @SerializedName("daytime_movement") var dayTimeMovement: List<Int>? = null
) : OreoUserActivitiesPost() {
    data class HourlyBreakup(
        @SerializedName("steps") var totalSteps: Int,
        @SerializedName("active_calories") var activeCalories: Int,
        @SerializedName("calories") var totalCalories: Int,
        @SerializedName("distance") var totalDistance: Int,
        @SerializedName("hour_of_the_day") var hourOfDay: Int
    ) : OreoUserActivitiesPost()

    data class DayBreakup(
        @SerializedName("total_steps") var totalSteps: Int,
        @SerializedName("active_calories") var activeCalories: Int,
        @SerializedName("total_calories") var totalCalories: Int,
        @SerializedName("total_distance") var totalDistance: Int,
        @SerializedName("date") var date: String
    ) : OreoUserActivitiesPost()
}

data class OreoSleepNetworkEntity(
    @SerializedName("hourly_breakup") var hourlyBreakup: List<OreoHourlyBreakup>? = null,
    @SerializedName("night_time_movement") var nightTimeMovement: List<OreoMovementBreakup>? = null,
    @SerializedName("day_break_up") var dayBreakup: OreoDayBreakup? = null
) : OreoUserActivitiesPost() {

    data class OreoHourlyBreakup(
        @SerializedName("start_time") var startTime: String,
        @SerializedName("sleep_type") var sleepType: String,
        @SerializedName("duration") var duration: Int,
        @SerializedName("end_time") var endTime: String,

        ) : OreoUserActivitiesPost()

    data class OreoMovementBreakup(
        @SerializedName("start_time") var startTime: String,
        @SerializedName("movement_type") var movementType: String,
        @SerializedName("duration") var duration: Int,
        @SerializedName("end_time") var endTime: String,

        ) : OreoUserActivitiesPost()

    data class OreoDayBreakup(
        @SerializedName("total_deep") var totalDeep: Int,
        @SerializedName("total_light") var totalLight: Int,
        @SerializedName("total_duration") var totalDuration: Int,
        @SerializedName("time_in_bed") var timeInBed: Int,
        @SerializedName("start_time") var startTime: String,
        @SerializedName("end_time") var endtime: String,
        @SerializedName("total_awake") var totalAwake: Int,
        @SerializedName("date") var date: String,//05/15/2023
        @SerializedName("total_rem") var totalRem: Int,
        @SerializedName("sleep_score") var sleepScore: Int,
        @SerializedName("readiness_score") var readinessScore: Int,
        @SerializedName("avg_hrv") var avgHrv: Int,
        @SerializedName("sleep_efficiency") var sleepEfficiency: Int,
        @SerializedName("resting_hr") var restingHr: Int,
        @SerializedName("sleep_latency") var sleepLatency: Int,
        @SerializedName("max_temp") var maxTemp: Float,
        @SerializedName("avg_resp") var avgResp: Int,
        @SerializedName("hr_breakup") var hrBreakup: List<Int>,
        @SerializedName("temp_breakup") var tempBreakup: List<Float>,
        @SerializedName("resp_breakup") var respBreakup: List<Int>,
        @SerializedName("hrv_breakup") var hrvBreakup: List<Int>,
    ) : OreoUserActivitiesPost()

}

data class OreoCommonNetworkEntity(
    @SerializedName("day_break_up") var dayBreakup: DayBreakup? = null
) : OreoUserActivitiesPost() {

    data class DayBreakup(
        @SerializedName("break_up") var breakUp: List<Int>? = null,
        @SerializedName("frequency") var frequency: Int,
        @SerializedName("date") var date: String
    ) : OreoUserActivitiesPost()
}

data class OreoBodyTempNetworkEntity(
    @SerializedName("day_break_up") var dayBreakup: DayBreakup? = null
) : OreoUserActivitiesPost() {

    data class DayBreakup(
        @SerializedName("break_up") var breakUp: List<Float>? = null,
        @SerializedName("frequency") var frequency: Int,
        @SerializedName("date") var date: String
    ) : OreoUserActivitiesPost()
}

data class OreoHeartNetworkEntity(
    @SerializedName("day_break_up") var dayBreakup: DayBreakup? = null
) : OreoUserActivitiesPost() {

    data class DayBreakup(
        @SerializedName("break_up") var breakUp: List<Int>? = null,
        @SerializedName("frequency") var frequency: Int,
        @SerializedName("date") var date: String
    ) : OreoUserActivitiesPost()
}