package com.noisefit_commans.data.model

import com.google.gson.annotations.SerializedName

data class UserModal(
    @SerializedName("users") val userList:List<User> ?=null
)