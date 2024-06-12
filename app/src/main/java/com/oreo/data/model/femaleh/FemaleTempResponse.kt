package com.oreo.data.model.femaleh

data class FemaleTempResponse(
    val nudge: Nudge? = null,
    val temp: List<TempPeriodData>? = null
)