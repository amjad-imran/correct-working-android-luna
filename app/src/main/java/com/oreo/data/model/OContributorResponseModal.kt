package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class OContributorResponseModal(
    @SerializedName("total_sleep")
    val totalSleep: String? = null,
    val efficiency: String? = null,
    val restfulness: String? = null,
    @SerializedName("rem_sleep")
    val remSleep: String? = null,
    val sleep_score: String? = null,
    val readiness_score: String? = null,//check
    val heart_rate_variability: String? = null,//check
    val skin_temperature: String? = null,//check
    val respiratory_rate: String? = null,//check
    val activity_score: String? = null,//check
    val activity_goal: String? = null,//check
    val total_burn: String? = null,//check
    val hr_graph: String? = null,//check
    val hrv_graph: String? = null,//check
    val temp_graph: String? = null,//check
    val steps: String? = null,//check
    val distance: String? = null,//check
    val daytime_movement: String? = null,//check
    val time_in_bed: String? = null,
    @SerializedName("deep_sleep")
    val deepSleep: String? = null,
    val latency: String? = null,
    val timing: String? = null,
    @SerializedName("stay_active")
    val stayActive: String? = null,
    @SerializedName("move_every_hour")
    val moveEveryHour: String? = null,
    @SerializedName("training_frequency")
    val trainingFrequency: String? = null,
    @SerializedName("training_volume")
    val trainingVolume: String? = null,
    @SerializedName("calories_goal")
    val caloriesGoal: String? = null,
    @SerializedName("yesterday_sleep_duration")
    val yesterdaySleepDuration: String? = null,
    @SerializedName("sleep_balance")
    val sleepBalance: String? = null,
    @SerializedName("yesterday_activity")
    val yesterdayActivity: String? = null,
    @SerializedName("activity_balance")
    val activityBalance: String? = null,
    @SerializedName("hrv_balance")
    val hrvBalance: String? = null,
    @SerializedName("resting_hr")
    val restingHr: String? = null,//check
    val average_hr: String? = null,//check
    @SerializedName("heart_rate")
    val heartRate: String? = null,
    @SerializedName("recovery_index")
    val recoveryIndex: String? = null

)