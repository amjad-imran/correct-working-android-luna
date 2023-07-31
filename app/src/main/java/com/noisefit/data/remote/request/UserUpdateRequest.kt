package com.noisefit.data.remote.request

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo

data class UserUpdateRequest(
    @SerializedName("user") var user: UserUpdate? = null,
    @SerializedName("user_info") var userInfo: UserInfo? = null,
    @SerializedName("user_goals") var userGoals: UserGoals? = null
)

data class UserUpdate(
    @SerializedName("image_url") var imageUrl: String? = null,
    @SerializedName("last_name") var lastName: String? = null,
    @SerializedName("first_name") var firstName: String? = null
)