package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contributors(
    val title: String,
    val leftText: String,
    val leftTextColor: Int,
    val barColor: Int,
    var barPercent: Int,
    val backgroundRes: Int,
    var description:String=""
):Parcelable