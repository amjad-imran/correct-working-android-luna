package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class OSleepInternalTrendsDataModel(
    @SerializedName("values")
    var values: List<TrendsValues>? = null
)

data class TrendsValues(
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("value")
    val value: Int? = null,
    @SerializedName("value2")
    val value2: Int? = null,
    @SerializedName("nudge")
    val nudge: String? = null
)