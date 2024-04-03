package com.oreo.data.model

data class OWDActivityHRZoneData(
    val title: String,
    val color: String,
    val range : String,
    val percentage : Int,
    var isHighlighted : Boolean = true,
    val duration : String)
