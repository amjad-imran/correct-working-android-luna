package com.noisefit_commans.models

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomWatchFace(
    val backgroundLayer: Int,
    val textLayer: Int,
    val id: Int,
    var binFile: Int,
    var color: Int? = null,
    var selectedImagePath: String? = null,
    var layout: Int = 0,

) : Parcelable

@Parcelize
data class DiyCustomWatchFace(
    var binFile: String? = null,
    var textLayerName: String? = null,
    var textLayer: Bitmap? = null,
    var textLayerStyle: Int = 0,
    var image: Bitmap? = null,
    var color: Int? = null,
    var screenType: String,
    var width:Int,
    var height:Int
) : Parcelable