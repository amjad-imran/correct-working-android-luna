package com.oreo.data.model.circadian

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CircadianQuizResponseModel(
    val id: Int?,
    val text: String?,
    val answer: List<CircadianQuizOptionsModel>?,
    var selectedOptionId: Int ?= null
) : Parcelable

@Parcelize
data class CircadianQuizOptionsModel(
    val id: Int?,
    val text: String?,
    val score: Int?,
) : Parcelable