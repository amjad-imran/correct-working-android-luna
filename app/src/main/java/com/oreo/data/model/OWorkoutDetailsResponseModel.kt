package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class OWorkoutDetailsResponseModel(
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("device_id")
    val deviceId: String,
    val duration: Long? = null,
    val calories: Int? = null,
    val hrArray: List<Int>? = null,
    val steps: Int? = null,
    @SerializedName("activity_type")
    val activityType: String? = null,
    @SerializedName("start_time")
    val startTime: String? = null,
    @SerializedName("end_time")
    val endTime: String? = null,
    val intensity: String? = null,
    @SerializedName("created_date")
    val createdDate: String? = null,
    val date: String?=null,
    @SerializedName("hr_avg")
    val hrAvg: Int? = null,
    @SerializedName("hr_low")
    val hrLow: Int? = null,
    @SerializedName("icon_url")
    val iconUrl: String? = null
)