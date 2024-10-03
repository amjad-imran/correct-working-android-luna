package com.noisefit.data.model.referral

import com.google.gson.annotations.SerializedName

data class ReferralCodeResponse(
    @SerializedName("referral_code")
    val referralCode: String? = null,
    @SerializedName("share_message")
    val shareMessage: String? = null,
)
