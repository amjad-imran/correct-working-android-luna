package com.noisefit_commans.data.model


data class StressZoneAnalysis(
    val type: String,
    val range: String,
    val percentage: Int,
    val stressType: StressType
)

enum class StressType {
    Relax,
    Normal,
    Medium,
    High
}