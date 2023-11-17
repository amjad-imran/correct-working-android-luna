package com.oreo.data.model

import com.noisefit_commans.models.ColorfitData
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.model.health.OreoDashboardResponseModel
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.model.health.OreoSleepModel


data class ServerUserHealthData(
    var dashboard: List<OreoDashboardResponseModel>? = null,
    var sleep: List<OreoSleepModel>? = null,
    var activity: List<OreoActivityModel>? = null,
    var readiness: List<OreoReadinessModel>? = null,
) : ColorfitData()