package com.oreo.data.model

data class OWDActivityHRZoneData(
    val title: String,
    val zone: Int,//0..5
    val color: String,
    val range: String,
    val percentage: Int,
    var isHighlighted: Boolean = false,
    var isDisable: Boolean = false,
    val duration: String,
    val dataSize: Int,
    val selectedIndexes: List<Int>
)
