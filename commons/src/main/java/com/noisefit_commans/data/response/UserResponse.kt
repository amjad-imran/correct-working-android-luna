package com.noisefit_commans.data.response

import com.noisefit_commans.data.model.Token
import com.noisefit_commans.data.model.User


data class UserResponse(
    val user: User?,
    val token: Token?,
)