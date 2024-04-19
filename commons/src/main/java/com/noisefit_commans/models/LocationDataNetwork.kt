package com.noisefit_commans.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationDataNetwork(
    val lat: Double? = null,
    val long: Double? = null,
    val timestamp: Long? = null
) : Parcelable