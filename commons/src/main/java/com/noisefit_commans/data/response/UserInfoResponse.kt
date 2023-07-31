package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo

data class UserInfoResponse (
    @SerializedName("user_info") var userInfo: UserInfo? = null,
    @SerializedName("user_goals") var userGoals: UserGoals? = null
)