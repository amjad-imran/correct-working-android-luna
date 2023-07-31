package com.noisefit_commans.data.model


import com.google.gson.annotations.SerializedName

data class Buddy(
    @SerializedName("auth_token")
    val authToken: String,
    @SerializedName("can_nudge")
    var canNudge: Boolean,
    @SerializedName("email")
    val email: String,
    @SerializedName("enable_nudge")
    var enableNudge: Boolean,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("last_step_activity")
    val lastStepActivity: LastStepActivity,
    @SerializedName("mobile")
    val mobile: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("otherUserDatas")
    val otherUserDatas: List<OtherUserData>,
    @SerializedName("push_token")
    val pushToken: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("lastUpdatedTime")
    val lastUpdatedTime: String?="",
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("total_trophies")
    val totalTrophies: Int,
    @SerializedName("info")
    val info: String? = null
)

data class OtherUserData(
    val count: Int? = 0,
    val key: String? = "",
    val label: String? = "",
    val status: String? = "",
    val duration: Int? = 0
)