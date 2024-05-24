package com.oreo.ui.custom

data class TempPeriodCombineModel(
    val sections: List<Section>? = null,
    val items: List<ItemTemp>? = null
)

data class ItemTemp(
    val value: Float? = null,
    val index: Int,
    val date: String//yyyy-MM-dd
)