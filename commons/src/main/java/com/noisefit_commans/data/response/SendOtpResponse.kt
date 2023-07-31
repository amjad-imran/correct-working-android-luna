package com.noisefit_commans.data.response

data class SendOtpResponse(
    val message: String,
    val status: Int? = null,
    val is_pwd: Boolean? = false,
    val mask_email: String? = null,
    val image_url: String? = null,
    val mobile: String? = null
)