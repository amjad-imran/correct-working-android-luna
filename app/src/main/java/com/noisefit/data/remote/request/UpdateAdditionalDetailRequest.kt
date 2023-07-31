package com.noisefit.data.remote.request

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo

data class UpdateAdditionalDetailRequest (
    @SerializedName("user_goals") val stepGoal: UserGoals?=null,
    @SerializedName("user_info") val userInfo: UserInfo?=null
    )