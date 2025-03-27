package com.oreo.ui.customHomeScreen

data class CustomHomeScreenItem(
    val icon: Int,
    val key: String,
    val title: String,
    var switchState: Boolean,
    var priority: Int
)
