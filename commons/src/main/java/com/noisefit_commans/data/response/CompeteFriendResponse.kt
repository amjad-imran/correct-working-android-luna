package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName


data class CompeteFriendResponse(
    @SerializedName("title")
    val rule: String? = null,
    @SerializedName("finalData")
    val competeFriend: List<CompeteFriend>? = null
)


data class CompeteFriend(
    @SerializedName("friend_name")
    val name: String? = null,
    @SerializedName("friend_image")
    val url: String? = null,
    @SerializedName("status")
    var status: String? = null,
    @SerializedName("user_interest")
    val interest: List<String>? = null,
    @SerializedName("comp_id")
    var compId: Int? = null,
    @SerializedName("friend_id")
    val id: Int? = null,
)