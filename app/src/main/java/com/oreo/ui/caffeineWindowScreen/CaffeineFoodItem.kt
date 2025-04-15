package com.oreo.ui.caffeineWindowScreen

data class CaffeineFoodItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val unit: String,
    val portion_size: Double ?,
    val portion_unit: String ?,
    var is_favorite: Boolean ?= false
)