package com.oreo.data.model.referral

data class ReferralsMain(
    val referralName: String? = null,
    val referrals: List<Referral>? = null,
    val status: String? = null, //won,lost,pending
    val pendingMessage: String? = null
)

data class Referral(
    val name: String? = null,
    val status: String? = null,//purchased,delivered
)
