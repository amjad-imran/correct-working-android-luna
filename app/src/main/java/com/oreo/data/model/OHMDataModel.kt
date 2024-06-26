package com.oreo.data.model

data class OHMDataModel(
    val icon: Int,
    val title: String,
    val value: String? = null,
    val rangeValue: String? = null
)