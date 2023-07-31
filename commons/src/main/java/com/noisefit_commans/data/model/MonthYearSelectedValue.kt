package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MonthYearSelectedValue(
    var month : Int,
    var year : Int,
    val onlyYear: Boolean
) : Parcelable