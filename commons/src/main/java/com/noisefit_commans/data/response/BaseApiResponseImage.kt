package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class BaseApiResponseImage(
    @SerializedName("image_url")
    val imageUrl: String?=null
)