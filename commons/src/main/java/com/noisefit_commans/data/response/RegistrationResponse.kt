package com.noisefit_commans.data.response

import com.noisefit_commans.data.model.Token
import com.noisefit_commans.data.model.User

data class RegistrationResponse(
    var user: User?,
    var token: Token? = null,
    var social_data: SocialData?
)

data class SocialData(
    val sub: String? = null,
    val email: String? = null,
    val id: String? = null
)