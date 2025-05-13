package com.oreo.data.model

import android.os.Parcelable
import com.noisefit_commans.data.model.HrAlert
import kotlinx.parcelize.Parcelize

@Parcelize
data class IrregularEventsChipsListModel(
    val irregularEventsChipsList: List<IrregularEventsChipModel>,
    val alert: HrAlert
) : Parcelable

@Parcelize
data class IrregularEventsChipModel(
    val key:String,
    val displayName:String
) : Parcelable
