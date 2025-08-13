package com.oreo.data.model.timeline

import com.google.gson.annotations.SerializedName

data class TimelineScreenResponse(
    val timeTracker: List<ItemTimelineResponseModel>?
)

data class ItemTimelineResponseModel(
    @SerializedName("user_id")
    val userId: Int?,

    @SerializedName("event")
    val event: String?,

    @SerializedName("start_date")
    val startDate: String?,

    @SerializedName("start_time")
    val startTime: String?,

    @SerializedName("end_date")
    val endDate: String?,

    @SerializedName("end_time")
    val endTime: String?,

    @SerializedName("value")
    val value: String?,

    @SerializedName("unit")
    val unit: String?,

    @SerializedName("date")
    val date: String?,

    val title: String?,

    // for app
    var titleColor: Int?=null,
    var desc: String?="-",
    var displayTime: String?
)