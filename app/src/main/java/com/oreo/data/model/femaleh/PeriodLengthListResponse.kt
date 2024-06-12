package com.oreo.data.model.femaleh

data class PeriodLengthListResponse(
    val length: List<PeriodLength>,
    val avg: Int? = null,
    val nudge: Nudge? = null,
)

data class Nudge(
    val message: String? = null
)

data class PeriodLength(
    val date: String,
    val length: Int = 0,
    val nudge: String? = null
)