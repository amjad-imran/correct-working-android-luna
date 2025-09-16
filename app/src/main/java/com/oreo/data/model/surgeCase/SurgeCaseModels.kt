package com.oreo.data.model.surgeCase

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AboutSurgeCaseModel(
    val title: String,
    val description: String,
    val imageCenter: Int?=null,
    val imageTopSticked: Int?=null,
    val imageClosedCharger: Int?=null,
) : Parcelable