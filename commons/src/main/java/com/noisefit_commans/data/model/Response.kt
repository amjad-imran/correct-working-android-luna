package com.noisefit_commans.data.model


import com.google.gson.annotations.SerializedName

data class Response(
    @SerializedName("users")
    val users: List<User>
)