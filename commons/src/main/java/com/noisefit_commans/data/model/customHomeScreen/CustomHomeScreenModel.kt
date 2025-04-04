package com.noisefit_commans.data.model.customHomeScreen

import com.google.gson.annotations.SerializedName

data class CustomHomeScreenModel(
    @SerializedName("manage")
    val manage: Boolean? = null,
    val type: String,
    val cards: List<CustomHomeScreenNetworkItem>
)