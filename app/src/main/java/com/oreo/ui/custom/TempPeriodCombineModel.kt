package com.oreo.ui.custom

import com.oreo.ui.femalehealth.cycletracker.CyclePhase

data class TempPeriodCombineModel(
    val sections: List<Section>? = null,
    val items: List<ItemTemp>? = null,
    val maxValue: Float
)

data class ItemTemp(
    val value: Float? = null,
    var phase: CyclePhase = CyclePhase.FOLLECULAR,
    val index: Int,
    val date: String//yyyy-MM-dd
)