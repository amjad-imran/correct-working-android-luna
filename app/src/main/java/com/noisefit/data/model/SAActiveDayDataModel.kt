package com.noisefit.data.model

data class SAActiveDayDataModel(
    val name: String,
    var isSelected: Boolean = false,
    var isPreSelected: Boolean = false,
    val dayKey: Int//Calendar.MONDAY
)
