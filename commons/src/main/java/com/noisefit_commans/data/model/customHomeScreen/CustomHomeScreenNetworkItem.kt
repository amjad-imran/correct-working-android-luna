package com.noisefit_commans.data.model.customHomeScreen

import com.google.gson.annotations.SerializedName

data class CustomHomeScreenNetworkItem(
    val type: String,
    @SerializedName("switch_state")
    var switchState: Boolean,
    var priority: Int
)