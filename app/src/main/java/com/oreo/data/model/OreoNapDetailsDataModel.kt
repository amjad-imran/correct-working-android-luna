package com.oreo.data.model

import com.google.gson.annotations.SerializedName
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.UnitDataModelArray
import com.oreo.data.model.health.UnitDataModelArrayFloat

data class OreoNapDetailsDataModel(
    @SerializedName("start_time")
    var startTime: String,
    @SerializedName("end_time")
    var endTime: String,
    @SerializedName("prev_sleep_score")
    var prevSleepScore: Int? = null,
    @SerializedName("sleep_score")
    var sleepScore: Int? = null,
    var date: String? = null,
    @SerializedName("prev_readiness_score")
    var prevReadinessScore: Int? = null,
    @SerializedName("readiness_score")
    var readinessScore: Int? = null,
    var duration: Long? = null,
    var id: String? = null,
    var title: String? = null,

    @SerializedName("score_impact")//positive/negative
    var scoreImpact: String? = null,

    @SerializedName("sub_title")
    var subtitle: String? = null,
    var nudges: List<Nudges>? = null,
    var hrBreakup: UnitDataModelArray? = null,
    var hrvBreakUp: UnitDataModelArray? = null,
    var temperatureBreakup: UnitDataModelArrayFloat? = null,
    var na: NotAvailableContent? = null
)

data class NotAvailableContent(
    val title: String? = null,
    val text: String? = null
)