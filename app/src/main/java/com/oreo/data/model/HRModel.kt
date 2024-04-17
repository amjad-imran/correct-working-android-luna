package com.oreo.data.model


data class HRModel(
    val maxValues: Int,
    val minValues: Int,
    val values: List<Int>? = null,
    val midValues: Int,
)