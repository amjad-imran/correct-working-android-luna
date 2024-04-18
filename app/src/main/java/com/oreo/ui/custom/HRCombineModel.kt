package com.oreo.ui.custom;


data class HRCombineModel(
    val sections: List<Section>? = null,
    val items: List<Item>? = null
)

/*data class Item(
    val `value`: Int,
    val index: Int,
    val minValue:Int=0,
    val maxValue:Int=0
)

data class Section(
    var type: String,
    var start: Int,
    var end: Int,
    val color: Int,
    val imageRes: Int,
    val imageUrl: String? = null,
    var count: Int? = null
)*/
