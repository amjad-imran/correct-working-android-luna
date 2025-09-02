package com.oreo.data.model

data class CircadianGraphModel(
    val xAxis: String? = null,
    var firstMidPoint: CircadianMidPointModel? = null,
    var secondMidPoint: CircadianMidPointModel? = null,
    var bothMidPoint: Pair<CircadianMidPointModel?, CircadianMidPointModel?>? = null,
)

data class TimeWindow(
    val startHour: Float,  // e.g., 9.0f or 13.5f
    val endHour: Float,
    val startColor: Int,
    val endColor:Int,
    val textColor:Int,
    val rowIndex: Int = 0,
    val label: String = ""
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
    None
}

enum class CircadianMidPointStatus {
    Locked,
    Maintained,
    Worsening,
    Correcting,
    SleepMissing,
    AwaitingSync,
    FAILED
}