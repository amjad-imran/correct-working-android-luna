package com.noisefit.data.model

data class SAActiveDayDataModel(
    var isSelected: Boolean = false,
    var isPreSelected: Boolean = false,
    val dayKey: Int//Calendar.MONDAY
)
