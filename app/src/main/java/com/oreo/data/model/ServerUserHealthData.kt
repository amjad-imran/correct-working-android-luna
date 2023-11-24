package com.oreo.data.model

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.ColorfitData
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.model.health.OreoDashboardResponseModel
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.data.model.health.WelcomeData


data class ServerUserHealthResponse(
    val data: List<ServerUserHealthData>,

    @SerializedName("activitityScoreAvg")
    val activityScoreAvg: ODashboardActivityScoreModel? = null,
    @SerializedName("sleepScoreAvg")
    val sleepScoreAvg: ODashboardSleepScoreModel? = null,
    @SerializedName("readinessScoreAvg")
    val readinessScoreAvg: ODashboardReadinessScoreModel? = null,
    @SerializedName("registerDate")
    val registerDate: Int? = null,
    val welcome: WelcomeData? = null,
) : ColorfitData()

data class ServerUserHealthData(
    val date: String,//YYYY-MM-dd
    var dashboard: OreoDashboardResponseModel? = null,
    var sleep: OreoSleepModel? = null,
    var activity: OreoActivityModel? = null,
    var readiness: OreoReadinessModel? = null,
    var heart: DataBreakup? = null,


)

data class DataBreakup(
    val break_up:List<Int>
)
