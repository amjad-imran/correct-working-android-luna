package com.noisefit.data.model

data class RingLocationData(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val battery_percentage: Int? = null,
    val last_sync: String? = null,
    var address:String?=null
)