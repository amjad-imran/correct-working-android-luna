package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RingLocationData(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val battery_percentage: Int? = null,
    val last_sync: String? = null,
    var address: String? = null
) : Parcelable