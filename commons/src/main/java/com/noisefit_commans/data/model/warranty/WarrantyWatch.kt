package com.noisefit_commans.data.model.warranty

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WarrantyWatch(
    val product_id: Int,
    val product_name: String
) : Parcelable
