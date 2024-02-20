package com.oreo.data.model


data class DayTimeDataModel(
    val sections: List<Section>? = null,
    val items: List<Item>? = null
)

data class Item(
    val `value`: Int,
    val index: Int
)

data class Section(
    var type: String,
    var start: Int,
    var end: Int,
    val color: Int,
    val imageRes: Int,
    val imageUrl: String? = null,
    var count: Int? = null
)