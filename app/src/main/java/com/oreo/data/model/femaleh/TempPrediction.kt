package com.oreo.data.model.femaleh

data class TempPrediction(
    val tempVariation: Float? = null,
    val message: String? = null,
    val tempData: List<TempPeriodData>
)

data class TempPeriodData(
    val date: String,
    val temperature: Float? = null)