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

    @SerializedName("event_id")
    val eventId: String?=null,

    @SerializedName("event")
    var event: String?,

    @SerializedName("start_date")
    val startDate: String?,

    @SerializedName("start_time")
    var startTime: String?,

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

    var title: String?,

    val metadata: TimelineMetadata ?= null,

    // for app
    var titleColor: Int?=null,
    var desc: String?="-",
    var displayTime: String?,

    /*
        0 -> No edit, Only Visible
        1 -> Edit
        2 -> Delete
        3 -> Edit and Delete
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

    @SerializedName("luna_option")
    val lunaOption: String? = null,

    @SerializedName("luna_tracking_option_id")
    val lunaTrackingOptionId: Int? = null,

    @SerializedName("luna_options")
    val lunaOptions: List<String>? = null,

    @SerializedName("luna_tracking_option_ids")
    val lunaTrackingOptionIds: List<Int>? = null,

    @SerializedName("end_time")
    val endTime: String? = null,

    val foods: List<MealAiFoods>?=null,
    val macros: List<MealAiMacros>?=null,
    val prompt: String?=null,

    // For Periods
    val symptoms: List<String> ?= null,
    val flow: List<String> ?= null,
    val flow_type: String ?= null,

    // For Workout
    val type: String? = null,
    val steps: Int? = null,
    val calories: Int? = null,
    val intensity: String? = null,
    @SerializedName("activity_type")
    val activityType: String? = null,
    @SerializedName("extra_calories")
    val extraCalories: Int? = null

) : Parcelable