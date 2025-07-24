package com.oreo.data.model

data class CircadianGraphModel(
    val xAxis: String? = null,
    var firstMidPoint: CircadianMidPointModel? = null,
    var secondMidPoint: CircadianMidPointModel? = null,
    var bothMidPoint: Pair<CircadianMidPointModel?,CircadianMidPointModel?>? = null,
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
    AwaitingSync
}