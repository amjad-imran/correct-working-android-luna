package com.noisefit_commans.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class ChallengeListResponse(
    @SerializedName("joinedChallenges") val joinedChallenges: ArrayList<com.noisefit_commans.data.response.Challenge>? = null,
    @SerializedName("otherChallenges") val otherChallenges: ArrayList<com.noisefit_commans.data.response.Challenge>? = null,
    @SerializedName("currentTime") val currentTime: String = ""
)

@Parcelize
data class Challenge(
    @SerializedName("id") val id: Int,
    @SerializedName("challengeType") val challengeType: String? = "",
    @SerializedName("bannerUrl") val bannerUrl: String? = "",
    @SerializedName("active") val active: Boolean,
    @SerializedName("status") val status: String? = "",
    @SerializedName("title") val title: String? = "",
    @SerializedName("subtitle") val subtitle: String? = "",
    @SerializedName("startDate") val startDate: String? = "",
    @SerializedName("endDate") val endDate: String? = "",
    @SerializedName("target") val target: Double,
    @SerializedName("progress") val progress: Double,
    @SerializedName("participants") var participants: Int,
    @SerializedName("currentTime") var currentTime: String = ""
) : Parcelable