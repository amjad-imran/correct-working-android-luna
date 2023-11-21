package com.oreo.data.model

import com.noisefit_commans.models.ColorfitData
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.model.health.OreoDashboardResponseModel
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.model.health.OreoSleepModel


data class ServerUserHealthResponse(
    val data: List<ServerUserHealthData>,
    val register_date: Int,
) : ColorfitData()

data class ServerUserHealthData(
    val date: String,//YYYY-MM-dd
    var dashboard: OreoDashboardResponseModel? = null,
    var sleep: OreoSleepModel? = null,
    var activity: OreoActivityModel? = null,
    var readiness: OreoReadinessModel? = null,
)
