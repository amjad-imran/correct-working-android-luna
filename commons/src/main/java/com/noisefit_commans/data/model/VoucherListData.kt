package com.noisefit_commans.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VoucherListData(
    @SerializedName("user_voucher")
    @Expose
    val userVoucher: List<VoucherList>? = null
)
data class VoucherList(
    @SerializedName("id")
    @Expose
    val id: Int? = null,
    @SerializedName("brand")
    @Expose
    val brand: String? = null,
    @SerializedName("image_url")
    @Expose
    val imageUrl: String? = null
)