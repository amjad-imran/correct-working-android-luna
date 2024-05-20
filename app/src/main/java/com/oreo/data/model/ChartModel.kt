package com.oreo.data.model

open class ChartModel {
    var value = 0
    var valueFloat = 0.0f
    var index: String? = null
    var date: String? = null
    var isDistanceGraph: Boolean = false
    var isMetric: Boolean = true
    var formattedDate: String? = null
}

data class ChartModelStress(
    val calm: Int = 0,
    val focussed: Int = 0,
    val stressed: Int = 0,
    val index: String? = null,
    val date: String? = null,
    val isDistanceGraph: Boolean = false,
    val formattedDate: String? = null
)

data class PeriodChartModel(
    var value: Int = 0,
    var date: String? = null,
    var month: String? = null,
    var day: String? = null,
    var isNormal: Boolean = true
)