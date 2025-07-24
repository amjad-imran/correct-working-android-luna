package com.oreo.data.model.circadian

data class CircadianResponseModel(
    val activities: List<Activity>,
    val activity_monitor: List<ActivityMonitor>,
    val chronotype: Chronotype,
    val circadian_mid_point: CircadianMidPoint?,
    val graph_data: String?
//    val graph_data: GraphData?
)