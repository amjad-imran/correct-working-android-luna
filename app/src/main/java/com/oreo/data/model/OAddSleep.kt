package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OAddSleep(
    var title: String = "",
    var duration: Int = 0,
    var hour: String = "00",
    var minute: String = "00",
    var day: String = "",
    var unit: String = "",
) : Parcelable

