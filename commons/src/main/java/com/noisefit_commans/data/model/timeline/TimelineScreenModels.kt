package com.noisefit_commans.data.model.timeline

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

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

    val id: String?,

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

    val metadata: TimelineMetadata ?= null,

    // for app
    var titleColor: Int?=null,
    var desc: String?="-",
    var displayTime: String?,

    /*
        0 -> No edit, Only Visible
        1 -> Edit and Delete
        2 -> Edit
        3 -> Delete
    */
    var canBeEditedOrDeleted:Int = 0,
) : Parcelable

@Parcelize
data class TimelineMetadata(

    @SerializedName("sleep_score")
    val sleepScore: Int? = null,

    @SerializedName("readiness_score")
    val readinessScore: Int? = null,

    @SerializedName("prev_sleep_score")
    val prevSleepScore: Int? = null,

    @SerializedName("sleep_score_impact")
    val sleepScoreImpact: Int? = null,

    @SerializedName("prev_readiness_score")
    val prevReadinessScore: Int? = null,

    @SerializedName("readiness_score_impact")
    val readinessScoreImpact: Int? = null,

    @SerializedName("luna_tracking_option_id")
    val lunaTrackingOptionId: Int? = null,

    @SerializedName("end_time")
    val endTime: String? = null,

    val foods: List<MealAiFoods>?=null,
    val macros: List<MealAiMacros>?=null,
    val prompt: String?=null,

) : Parcelable