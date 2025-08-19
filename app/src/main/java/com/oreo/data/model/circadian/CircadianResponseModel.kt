package com.oreo.data.model.circadian

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.data.model.circadian.CircadianGraphData
import com.noisefit_commans.data.model.circadian.CircadianMidPointData

data class CircadianResponseModel(
    val activities: List<Activity>?,

    @SerializedName("activity_monitor")
    val activityMonitor: List<ActivityMonitor>?,

    val chronotype: Chronotype?,

    @SerializedName("circadian_mid_point")
    val circadianMidPoint: CircadianMidPointData?,

    @SerializedName("graph_data")
    val graphData: CircadianGraphData?,

    @SerializedName("is_locked")
    val isLockedCircularView: Boolean?
)

data class Activity(
    val goal: String?,
    val progress: Int?,
    val status: Boolean?,
    val time: Int?,
    val type: String
)

data class ActivityMonitor(
    val status: String?,
    val type: String
)

data class Chronotype(
    val description: String,
    val type: String
)