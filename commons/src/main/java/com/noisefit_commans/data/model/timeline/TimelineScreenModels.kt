package com.noisefit_commans.data.model.timeline

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Measurements(
    val hr: Boolean = true,
    val spo2: Boolean = true,
    val stress: Boolean = true,
    val temp: Boolean = true,
)

data class TimelineScreenResponse(
    val timeTracker: List<ItemTimelineResponseModel>?
)

@Parcelize
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
) : Parcelable