package com.oreo.data.model

data class CircadianGraphModel(
    val xAxis: String? = null,
    val firstMidPoint: CircadianMidPointModel? = null,
    val secondMidPoint: CircadianMidPointModel? = null,
    val secondMidPointTitle: String? = null
)

data class CircadianMidPointModel(
    var title: String? = null,
    var color: Int? = null,
    var bgColor: Int? = null,
    var index: Int? = null

)

enum class CircadianMidPointState {
    PhaseAligned,
    PhaseDelay,
    PhaseAdvance,
    SleepMissing,
    AwaitingSync
}