package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LearnMoreDataModel(
    val title: String? = null,
    val msg: String? = null,
    val img: Int? = null,
    val type: Int? = null
) : Parcelable