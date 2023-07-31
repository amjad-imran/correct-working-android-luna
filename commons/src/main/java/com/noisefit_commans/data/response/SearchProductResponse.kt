package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.data.model.ShopProduct

data class SearchProductResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("data")
    var data: List<ShopProduct>
)