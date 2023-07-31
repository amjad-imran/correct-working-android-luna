package com.noisefit.data.model

import android.graphics.ColorMatrix
import com.noisefit.ui.diy.FilterType


data class DiyCustomWatchFaceBg(
    var bgLink: String? = null,
    var textLayerLink: String? = null,
    val name: String = "",
    var isCustomImage: Boolean = false,
    var isSelected: Boolean = false,
    var color: Int = 0,
    var binUrl: String? = null,
    var colorMatrix: ColorMatrix? = null,
    var filterName: String? = null,
    var filterIntensity:Int = 50,
    var textLayerName: String? = null,
    var fontStyleName: String? = null,
    var sortIndex: Int? = null,
)

data class DiyCustomWatchType(
    var title: String,
    var isSelected: Boolean = false,
    var image: Int,
    var filterType: FilterType
)


data class DiyCustomWatchColor(
    var color: Int = 0,
    var isSelected: Boolean = false
)
