package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.data.model.CouponList


data class AllDealsResponse(
    val points: Long,
    @SerializedName("coupon_list") val couponList: List<CouponList>
)



