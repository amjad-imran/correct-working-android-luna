package com.noisefit_commans.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AvailCouponData(
    @SerializedName("code")
    @Expose
    val code: String? = null,
    @SerializedName("title")
    @Expose
    val title: String? = null,
    @SerializedName("message")
    @Expose
    val message: String? = null
)