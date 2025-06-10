package com.oreo.data.model

data class CaffeineFoodItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val unit: String,
    val portion_size: Double? = null,
    val portion_unit: String? = null,
    var is_favorite: Boolean? = false
)