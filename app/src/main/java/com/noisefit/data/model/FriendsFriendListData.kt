package com.noisefit.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FriendsFriendListData(
    @SerializedName("user_id")
    @Expose
    val userId: Int? = null,
    @SerializedName("image_url")
    @Expose
    val imageUrl: String? = null,
    @SerializedName("first_name")
    @Expose
    val firstName: String? = null,
    @SerializedName("interests")
    @Expose
    val interests: List<String>? = null,

    @SerializedName("status")
    @Expose
    var status: Int? = null,
    @SerializedName("is_request_received")
    @Expose
    val isRequestReceived: Boolean? = null
)
