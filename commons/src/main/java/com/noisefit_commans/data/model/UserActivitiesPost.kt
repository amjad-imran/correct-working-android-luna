package com.noisefit_commans.data.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

open class UserActivitiesPost {
    open fun serialize(): String = Gson().toJson(this)
}

data class UserDataPost(
    @SerializedName("version") var version: Int,
    @SerializedName("version_name") var versionName: String,
    @SerializedName("device_type") var deviceType: String? = null,
    @SerializedName("platform") var platform: String,
    @SerializedName("sleeps") var sleeps: List<SleepNetworkEntity>? = null,
    @SerializedName("steps") var steps: List<StepsNetworkEntity>? = null,
    @SerializedName("blood_oxygen") var bloodOxygen: List<CommonNetworkEntity>? = null,
    @SerializedName("stress") var stress: List<CommonNetworkEntity>? = null,
    @SerializedName("heart_rate") var heartRateHistory: List<HeartNetworkEntity>? = null,
    @SerializedName("body_temperature") var bodyTemperature: List<BodyTempNetworkEntity>? = null
) : UserActivitiesPost()


data class StepsNetworkEntity(
    @SerializedName("type") var type: String,
    @SerializedName("hourly_breakup") var hourlyBreakup: List<HourlyBreakup>? = null,
    @SerializedName("day_break_up") var dayBreakup: List<DayBreakup>? = null
) : UserActivitiesPost() {
    data class HourlyBreakup(
        @SerializedName("steps") var totalSteps: Int,
        @SerializedName("calories") var totalCalories: Int,
        @SerializedName("distance") var totalDistance: Int,
        @SerializedName("active_time") var totalActiveTime: Int,
        @SerializedName("date") var date: String,
        @SerializedName("hour_of_the_day") var hourOfDay: Int
    ) : UserActivitiesPost()

    data class DayBreakup(
        @SerializedName("steps") var totalSteps: Int,
        @SerializedName("calories") var totalCalories: Int,
        @SerializedName("distance") var totalDistance: Int,
        @SerializedName("active_time") var totalActiveTime: Int,
        @SerializedName("date") var date: String
    ) : UserActivitiesPost()
}

data class SleepNetworkEntity(
    @SerializedName("type") var type: String,
    @SerializedName("hourly_breakup") var hourlyBreakup: List<HourlyBreakup>? = null,
    @SerializedName("day_break_up") var dayBreakup: List<DayBreakup>? = null
) : UserActivitiesPost() {

    data class HourlyBreakup(
        @SerializedName("sleep_type") var sleepType: String,
        @SerializedName("duration") var duration: Int,
        @SerializedName("start_time") var startTime: String,
        @SerializedName("end_time") var endTime: String,
        @SerializedName("start_date") var startDate: String,
        @SerializedName("end_date") var endDate: String,
        @SerializedName("date") var date: String,
        @SerializedName("hour_of_the_day") var hourOfDay: Int
    ) : UserActivitiesPost()

    data class DayBreakup(
        @SerializedName("total_deep") var totalDeep: Int,
        @SerializedName("total_light") var totalLight: Int,
        @SerializedName("total_duration") var totalDuration: Int,
        @SerializedName("start_time") var startTime: String,
        @SerializedName("end_time") var endtime: String,
        @SerializedName("total_sober") var totalSober: Int,
        @SerializedName("total_awake") var totalAwake: Int,
        @SerializedName("date") var date: String,
        @SerializedName("rem_count") var totalRem: Int,
        @SerializedName("breath_quality") var breathQuality: Int,
        @SerializedName("sleep_score") var sleepScore: Int,
        @SerializedName("available_sleep_types") var availableSleepTypes: String,
        @SerializedName("hr_max") var hrMax: Int = 0,
        @SerializedName("hr_min") var hrMin: Int = 0,
        @SerializedName("hr_breakup") var heartRateBreakup: List<SleepHrData>?,
        @SerializedName("stress_max") var stressMax: Int = 0,
        @SerializedName("stress_min") var stressMin: Int = 0,
        @SerializedName("stress_breakup") var stressBreakUp: List<SleepStressData>?,
    ) : UserActivitiesPost()

}

data class CommonNetworkEntity(
    @SerializedName("type") var type: String,
    @SerializedName("hourly_break_up") var hourlyBreakup: List<HourlyBreakup>? = null,
    @SerializedName("day_break_up") var dayBreakup: List<DayBreakup>? = null
) : UserActivitiesPost() {

    data class HourlyBreakup(
        @SerializedName("date") var date: String,
        @SerializedName("time") var time: String,
        @SerializedName("value") var value: Int
    ) : UserActivitiesPost()

    data class DayBreakup(
        @SerializedName("count") var count: Int,
        @SerializedName("time") var time: String,
        @SerializedName("min_count") var minCount: Int,
        @SerializedName("max_count") var maxCount: Int,
        @SerializedName("date") var date: String
    ) : UserActivitiesPost()
}


data class BodyTempNetworkEntity(
    @SerializedName("type") var type: String,
    @SerializedName("hourly_break_up") var hourlyBreakup: List<HourlyBreakup>? = null,
    @SerializedName("day_break_up") var dayBreakup: List<DayBreakup>? = null
) : UserActivitiesPost() {

    data class HourlyBreakup(
        @SerializedName("date") var date: String,
        @SerializedName("time") var time: String,
        @SerializedName("value") var value: Float
    ) : UserActivitiesPost()

    data class DayBreakup(
        @SerializedName("count") var count: Float,
        @SerializedName("time") var time: String,
        @SerializedName("min_count") var minCount: Float,
        @SerializedName("max_count") var maxCount: Float,
        @SerializedName("date") var date: String
    ) : UserActivitiesPost()
}

data class HeartNetworkEntity(
    @SerializedName("type") var type: String? = null,
    @SerializedName("hourly_break_up") var hourlyBreakup: List<HourlyBreakup>? = null,
    @SerializedName("day_break_up") var dayBreakup: List<DayBreakup>? = null
) : UserActivitiesPost() {

    data class HourlyBreakup(
        @SerializedName("min") var min: Int,
        @SerializedName("max") var max: Int,
        @SerializedName("avg") var avg: Int,
        @SerializedName("hour_of_the_day") var hourOfDay: Int,
        @SerializedName("date") var date: String
    ) : UserActivitiesPost()

    data class DayBreakup(
        @SerializedName("min") var min: Int,
        @SerializedName("max") var max: Int,
        @SerializedName("avg") var avg: Int,
        @SerializedName("resting_hr") var restingHr: Int,
        @SerializedName("date") var date: String
    ) : UserActivitiesPost()
}