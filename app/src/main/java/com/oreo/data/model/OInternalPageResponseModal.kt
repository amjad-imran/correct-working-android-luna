package com.oreo.data.model

import com.google.gson.annotations.SerializedName

class OInternalPageResponseModal(
    val dateRange: String? = null,
    val result: List<ResultData>? = null,
    val trendData: TrendData? = null,
    val comparision: Comparison? = null
)

class ResultData(
    val date: String,
    val data: Float,
    val deviation: Float? = null,
    val year: String? = null
)

data class ResultDataStress(val date: String, val data: StressDataValues, val year: String? = null)
data class StressDataValues(
    val calm: Int = 0,
    val focussed: Int = 0,
    val stressed: Int = 0
)


class TrendData(
    val today: ValueData? = null,
    val yesterday: ValueData? = null,
    val allTimeAvg: Float? = null,
    val base: Float? = null
)

class ValueData(var value: Float = 0F)
class Comparison(
    var today: Int? = null,
    var average: Int? = null,
    var breakup: List<BreakUp>? = null

)

class BreakUp(
    @SerializedName("hour_of_day") var hourOfDay: Int? = null,
    var calories: Int? = null,
    @SerializedName("avg_calories") var avgCalories: Int? = null
)