package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LearnMoreDataModel(
    val label: String? = null,
    val msg: String? = null,
    val title: String? = null,
    val banner1: String? = null,
    val desc1: String? = null,
    val banner2: String? = null,
    val desc2: String? = null,
    val img: String? = null
):Parcelable