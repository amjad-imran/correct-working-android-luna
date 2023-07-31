package com.noisefit_commans.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ShopBannersModal(
    @SerializedName("img")
    @Expose
    val img: String? = null,
    @SerializedName("handle")
    @Expose
    val handle: String? = null,
    @SerializedName("url")
    @Expose
    val url: String? = null,
)