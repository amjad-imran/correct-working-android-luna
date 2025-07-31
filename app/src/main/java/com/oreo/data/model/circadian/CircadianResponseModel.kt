package com.oreo.data.model.circadian

import com.noisefit_commans.data.model.circadian.CircadianGraphData

data class CircadianResponseModel(
    val activities: List<Activity>,
    val activity_monitor: List<ActivityMonitor>,
    val chronotype: Chronotype,
    val circadian_mid_point: CircadianMidPoint?,
    val graph_data: CircadianGraphData?,
)

data class Activity(
    val goal: String?,
    val progress: Int?,
    val status: String?,
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

data class CircadianMidPoint(
    val avg_before: String,
    val circadian_midpoint: String,
    val end_time: String,
    val nudge: String,
    val start_time: String
)