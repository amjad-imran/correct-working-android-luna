package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Interest(
    val name: String,
    val id: Int? = -1
) : Parcelable
