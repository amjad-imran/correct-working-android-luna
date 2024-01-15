package com.oreo.data.model

import com.google.gson.annotations.SerializedName
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.UnitDataModelArray
import com.oreo.data.model.health.UnitDataModelArrayFloat

data class OreoNapDetailsDataModel(
    @SerializedName("start_time")
    var startTime: String,//YYYY-MM-DD,
    @SerializedName("end_time")
    var endTime: String,//YYYY-MM-DD,
    @SerializedName("prev_sleep_score")
    var prevSleepScore: Int? = null,
    @SerializedName("sleep_score")
    var sleepScore: Int? = null,
    @SerializedName("prev_readiness_score")
    var prevReadinessScore: Int? = null,
    @SerializedName("readiness_score")
    var readinessScore: Int? = null,
    var duration: Long? = null,
    var nudges: List<Nudges>? = null,
    var hrBreakup: UnitDataModelArray? = null,
    var hrvBreakUp: UnitDataModelArray? = null,
    var temperatureBreakup: UnitDataModelArrayFloat? = null,
)