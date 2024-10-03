package com.oreo.data.model.referral

import com.google.gson.annotations.SerializedName

data class ReferralsMain(
    @SerializedName("referral_name")
    val referralName: String? = null,
    val referred: List<Referral>? = null,
    val status: String? = null,
    val message: String? = null,
)

data class Referral(
    @SerializedName("used_by")
    val usedBy: String? = null,
    val status: String? = null,//purchased,delivered
    @SerializedName("created_date")
    val createdDate: String? = null
)
