package com.oreo.ui.caffeineWindowScreen

data class CaffeineFoodItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val portion_size: Double ?= 0.0,
    val portion_unit: String ?= "",
    var is_favorite: Boolean ?= false
)
