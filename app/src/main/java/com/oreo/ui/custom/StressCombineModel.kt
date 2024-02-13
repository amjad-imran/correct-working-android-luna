package com.oreo.ui.custom;


data class StressCombineModel(
    val sections: List<Section>? = null,
    val items: List<Item>? = null,
    val high: Int = 0,
    val medium: Int = 0
)

data class Item(
    val `value`: Int,
    val index: Int
)

data class Section(
    val start: Int,
    val end: Int,
    val color: Int,
    val imageRes: Int,
    val imageUrl: String?=null
)
