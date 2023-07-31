package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AGPSStatusState(
    var lastUpdated: Long? = null,
    var state: com.noisefit_commans.data.model.AGPSStatusEnum? = null
) : Parcelable

enum class AGPSStatusEnum(val status: String) {
    EXPIRED("expired"),
    UPDATED("updated"),
}