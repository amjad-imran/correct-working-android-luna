package com.oreo.data.model

data class SleepPlannerData(
    val planner: PlannerData? = null,
    val alarms: PlannerAlarmData? = null,
    val goal: String? = null,
)

data class PlannerData(
    val bed_time: String? = null,//24 hours format
    val wake_time: String? = null,//24 hours format
    val debt: Long? = null,//in seconds
    val duration: Long? = null,//in seconds
    val min_duration: Long? = null,//in seconds
    val nudge: String? = null,//in seconds
)

data class PlannerAlarmData(
    val mon: AlarmTimingsData? = null,
    val tues: AlarmTimingsData? = null,
    val wed: AlarmTimingsData? = null,
    val thur: AlarmTimingsData? = null,
    val fri: AlarmTimingsData? = null,
    val sat: AlarmTimingsData? = null,
    val sun: AlarmTimingsData? = null,
)

data class AlarmTimingsData(
    val bed_time: String? = null,
    val wake_time: String? = null,
)
