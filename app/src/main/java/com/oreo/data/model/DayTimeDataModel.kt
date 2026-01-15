package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class DayTimeDataModel(
    val sections: List<Section>? = null,
    val items: List<Item>? = null
) : Parcelable

@Parcelize
data class Item(
    var `value`: Int,
    val index: Int
) : Parcelable


@Parcelize
data class Section(
    var type: String,
    var start: Int,
    var end: Int,
    val color: Int,
    val imageRes: Int,
    val imageUrl: String? = null,
    var count: Int? = null
) : Parcelable