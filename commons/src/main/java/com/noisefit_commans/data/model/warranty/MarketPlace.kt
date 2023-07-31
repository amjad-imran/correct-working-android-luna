package com.noisefit_commans.data.model.warranty

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MarketPlace(
    val id: Int,
    val name: String
) : Parcelable
