package com.noisefit_commans.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UPIQRCode(
    var id: Int? = null,
    var title: String? = null,
    var url: String? = null
) : Parcelable