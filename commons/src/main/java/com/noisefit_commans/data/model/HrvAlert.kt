package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HrvAlerts(
    val date: String,
    var data: HrvAlert,
) : Parcelable

@Parcelize
data class HrvAlert(
    val spikePercent: Int=0,
    val minutes: Int,
    val currentValue: Int,
    val lastComparedValue: Int,
    var isDeleted : Boolean= false,
) : Parcelable