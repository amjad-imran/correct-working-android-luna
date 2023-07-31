package com.oreo.data.model

import com.google.gson.annotations.SerializedName

class OInternalPageResponseModal(
    val dateRange: String? = null,
    val result: List<ResultData>? = null,
    val trendData: TrendData? = null,
    val comparision: Comparison? = null
)

class ResultData(val date: String, val data: Float)
class TrendData(
    val today: ValueData? = null,
    val yesterday: ValueData? = null,
    val allTimeAvg: Float? = null
)

class ValueData(val value: Float = 0F)
class Comparison(
    var today: Int?=null,
    var average: Int?=null,
    var breakup: List<BreakUp>?=null

)

class BreakUp(
    @SerializedName("hour_of_day") var hourOfDay: Int?=null,
    var calories: Int?=null,
    @SerializedName("avg_calories") var avgCalories: Int?=null
)