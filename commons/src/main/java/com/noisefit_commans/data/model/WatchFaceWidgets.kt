package com.noisefit_commans.data.model

import android.os.Parcelable
import com.noisefit_commans.data.enums.GridType
import kotlinx.parcelize.Parcelize


@Parcelize
data class WatchFaceWidgets(
    val supportedGrid: List<GridType>,
    val image: Int,
    val widgetName: String,
    val isSelected: Boolean = false,
    val type: Int
) : Parcelable

