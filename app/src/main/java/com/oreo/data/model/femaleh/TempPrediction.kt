package com.oreo.data.model.femaleh

import com.google.gson.annotations.SerializedName

data class TempPrediction(
    val tempVariation: Float? = null,
    val message: String? = null,
    val tempData: List<TempPeriodData>
)

data class TempPeriodData(
    val date: String,
    @SerializedName("avg_temp")
    val temperature: Float? = null
)