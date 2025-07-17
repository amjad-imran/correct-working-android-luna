package com.oreo.data.model

data class CircadianGraphModel(
    val xAxis: String? = null,
    val firstMidPoint: Boolean = false,
    val firstMidPointTitle: String? = null,
    val secondMidPoint: Boolean = false,
    val secondMidPointTitle: String? = null)

enum class CircadianMidPointState{
    PhaseAligned,
    PhaseDelay,
    PhaseAdvance,
    SleepMissing,
    AwaitingSync
}