package com.noisefit.data.remote.request

data class LoginRequest(
    var login_type: String,
    var token: String? = null,
    var image_url: String? = null,
    var value: String? = null,
    var password: String? = null
)
