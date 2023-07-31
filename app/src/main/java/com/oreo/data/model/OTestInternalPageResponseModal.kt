package com.oreo.data.model

class OTestInternalPageResponseModal(
    val dateRange: String? = null,
    val result: List<ResultData>? = null,
    val trendData: TrendDatas? = null
)

class TrendDatas(val today: ValueData? = null, val yesterday: ValueData? = null,val allTimeAvg:Int?=null)
