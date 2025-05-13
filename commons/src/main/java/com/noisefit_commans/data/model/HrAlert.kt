package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HrAlerts(
    val date: String,
    var data: List<HrAlert>,
) : Parcelable

@Parcelize
data class HrAlert(
    val spikePercent: Int=0,
    val minutes: Int,
    val currentValue: Int,
    val lastComparedValue: Int,
    var isDeleted : Boolean= false,
) : Parcelable