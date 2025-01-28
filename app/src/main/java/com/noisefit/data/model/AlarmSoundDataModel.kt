package com.noisefit.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlarmSoundDataModel(
    val title: String,
    var isChecked: Boolean = false,
    val resId: Int,
    val key: Int
) : Parcelable
