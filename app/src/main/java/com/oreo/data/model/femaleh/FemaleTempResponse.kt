package com.oreo.data.model.femaleh

import com.google.gson.annotations.SerializedName

data class FemaleTempResponse(
    val nudge: Nudge? = null,
    val temp: List<TempPeriodData>? = null,
    @SerializedName("pending_nights")
    val pendingNights: Int? = null,
)