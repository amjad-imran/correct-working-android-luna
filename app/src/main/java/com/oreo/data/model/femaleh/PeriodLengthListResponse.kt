package com.oreo.data.model.femalehealth

import com.google.gson.annotations.SerializedName

data class PeriodLengthListResponse(
    val data: List<PeriodLength>
)

data class PeriodLength(
    val date: String,
    val length: Int = 0,
    val nudge: String? = null
)