package com.oreo.ui.caffeineWindowScreen

data class CaffeineFoodItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val unit: String,
    var is_favourite: Boolean = false
)
