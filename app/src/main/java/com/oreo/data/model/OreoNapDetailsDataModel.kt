package com.oreo.data.model

import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.UnitDataModelArray
import com.oreo.data.model.health.UnitDataModelArrayFloat

class OreoNapDetailsDataModel(

    var start_time: String="2023-12-12 04:06:30",//YYYY-MM-DD,
    var end_time: String="2023-12-12 04:06:30",//YYYY-MM-DD,
    var sleep_old_score: Int? = null,
    var sleep_new_score: Int? = null,
    var readiness_old_score: Int? = null,
    var readiness_new_score: Int? = null,
    var nap_duration: Long? = null,
    var nap_start_time: Long? = null,
    var nap_end_time: Long? = null,
    var nap_nudges: List<Nudges>? = null,
    var hrBreakUp: UnitDataModelArray? = null,
    var hrvBreakUp: UnitDataModelArray? = null,
    var temperatureBreakUp: UnitDataModelArrayFloat? = null,
)