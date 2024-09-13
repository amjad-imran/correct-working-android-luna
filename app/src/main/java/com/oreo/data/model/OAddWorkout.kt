package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class OAddWorkout(
    var duration: Int = 0,
    var steps: Int = 0,
    var calories: Int = 0,
    @SerializedName("extra_calories") var extraCalories: Int? = null,
    var startTimeIn24H: String = "",
    var endTimeIn24H: String = "",
    var intensity: String = "",
    var date: String? = null,
    @SerializedName("start_hour") var startHour: Int = 0,
    @SerializedName("start_minute") var startMinute: Int = 0,
    @SerializedName("end_hour") var endHour: Int? = 23,
    @SerializedName("end_minute") var endMinute: Int? = 59,
)

