package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IrregularEventsChipsListModel(
    val irregularEventsChipsList: List<IrregularEventsChipModel>
) : Parcelable

@Parcelize
data class IrregularEventsChipModel(
    val key:String,
    val displayName:String
) : Parcelable
