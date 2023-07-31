package com.noisefit_commans.data.model


import com.google.gson.annotations.SerializedName

data class BuddyUserModal(
    @SerializedName("email")
    val email: String?=null,
    @SerializedName("first_name")
    val firstName: String?=null,
    @SerializedName("image_url")
    val imageUrl: String?=null,
    @SerializedName("info")
    val info: Any?=null,
    @SerializedName("last_name")
    val lastName: String?=null,
    @SerializedName("mobile")
    val mobile: String?=null,
    @SerializedName("name")
    val name: String?=null,
    @SerializedName("status")
    val status: String?=null
)