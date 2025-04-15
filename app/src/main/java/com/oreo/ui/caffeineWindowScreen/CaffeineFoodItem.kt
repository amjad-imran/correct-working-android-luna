package com.oreo.ui.caffeineWindowScreen

data class CaffeineFoodItem(
    val id: Double,
    val name: String,
    val quantity: Double,
    val unit: String,
    val portion_size: Double ?,
    val portion_unit: String ?,
    var is_favorite: Boolean ?= false
)