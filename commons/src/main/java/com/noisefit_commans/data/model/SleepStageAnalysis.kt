package com.noisefit_commans.data.model

import com.noisefit_commans.models.SleepType


data class SleepStageAnalysis(
    val type: String,
    val timeInMinutes: Int,
    val percentage: Int,
    val sleepType: SleepType
)