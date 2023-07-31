package com.noisefit_commans.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.data.model.Buddy
import java.io.Serializable


data class ChallengeDetailResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("challengeType") val challengeType: String,
    @SerializedName("bannerUrl") val bannerUrl: String,
    @SerializedName("active") val active: Boolean? = null,
    @SerializedName("status") val status: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String? = null,
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null,
    @SerializedName("target") val target: Double = 0.0,
    @SerializedName("max_daily_target") val max_daily_target: Double? = null,
    @SerializedName("detail") val detail: String? = null,
    @SerializedName("shareText") val shareText: String? = null,
    @SerializedName("totalPrize") val totalPrize: Int? = null,
    @SerializedName("about") val about: String? = null,
    @SerializedName("trophyIcon") val trophyIcon: String? = null,
    @SerializedName("bannerUrlDetail") val bannerUrlDetail: String? = null,
    @SerializedName("sub_challenge_type") val sub_challenge_type: String? = null,
    @SerializedName("min_duration_target") val min_duration_target: Int? = null,
    @SerializedName("is_testing") val is_testing: Int? = null,
    @SerializedName("testing_users") val testing_users: String? = null,
    @SerializedName("is_team_challenge") val is_team_challenge: Boolean? = null,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null,
    @SerializedName("topPrizes") val topPrizes: List<com.noisefit_commans.data.response.TopPrizes>? = null,
    @SerializedName("achieverReward") val achieverReward: com.noisefit_commans.data.response.AchieverReward? = null,
    @SerializedName("participants") val participants: Int? = null,
    @SerializedName("achievers") val achievers: Int? = null,
    @SerializedName("teams") val teams: ArrayList<com.noisefit_commans.data.response.Teams>? = null,
    @SerializedName("userTeamDetails") val userTeamDetails: com.noisefit_commans.data.response.UserTeamDetails? = null,
    @SerializedName("joinedDetails") val joinedDetails: com.noisefit_commans.data.response.JoinedDetails? = null,
    @SerializedName("isJoined") val isJoined: Boolean? = null,
    @SerializedName("topThree") val topThreeList: List<com.noisefit_commans.data.response.TopThree>? = null,
    @SerializedName("currentTime") val currentTime: String? = null
)

data class AchieverReward(

    @SerializedName("id") val id: Int,
    @SerializedName("activity_challenge_id") val activity_challenge_id: Int,
    @SerializedName("note") val note: String,
    @SerializedName("msg") val msg: String
)

data class JoinedDetails(

    @SerializedName("userRankInTeam") val userRankInTeam: Int? = null,
    @SerializedName("teamRank") val teamRank: Int? = null,
    @SerializedName("userRank") val userRank: Int,
    @SerializedName("progress") val progress: Double,
    @SerializedName("trophyCollected") val trophyCollected: Boolean,
    @SerializedName("targetAchieved") val targetAchieved: Boolean
)

data class TopPrizes(

    @SerializedName("id") val id: Int,
    @SerializedName("activity_challenge_id") val activity_challenge_id: Int,
    @SerializedName("rank") val rank: Int,
    @SerializedName("msg") val msg: String,
    @SerializedName("prize") val prize: String,
    @SerializedName("rankText") val rankText: String,
    @SerializedName("image_url") val image_url: String
)

data class Teams(

    @SerializedName("id") val id: Int,
    @SerializedName("activity_challenge_id") val activity_challenge_id: Int,
    @SerializedName("team_name") val team_name: String,
    @SerializedName("image_url") val image_url: String,
    @SerializedName("participants") val participants: String? = null
) : Serializable

data class UserTeamDetails(

    @SerializedName("id") val id: Int,
    @SerializedName("activity_challenge_id") val activity_challenge_id: Int,
    @SerializedName("team_name") val team_name: String,
    @SerializedName("image_url") val image_url: String,
    @SerializedName("participants") val participants: String? = null
)

data class JoinChallengeRequest(
    @SerializedName("activityId") var id: String? = null,
    @SerializedName("teamId") var teamId: String? = null,
)

data class LeaveChallengeRequest(
    @SerializedName("activityId") var id: String? = null,
    @SerializedName("teamId") var teamId: String? = null,
    @SerializedName("comment") var comment: String = "",
)

data class AddBuddyRequest(
    @SerializedName("data") var data: ArrayList<com.noisefit_commans.data.response.AddBuddyIndividualRequest>? = null
)

data class AddBuddyIndividualRequest(
    @SerializedName("info") var info: String? = null,
    @SerializedName("mobile") var mobile: String? = null
)

data class InviteBuddyForChallengeRequest(
    @SerializedName("activityId") var activityId: String? = null,
    @SerializedName("mobile") var mobile: String? = null
)

data class BuddyListResponse(
    @SerializedName("data") var buddyList: ArrayList<Buddy>? = null

)

data class TopThree(
    @SerializedName("progress")
    @Expose
    var progress: Double? = null,

    @SerializedName("user_id")
    @Expose
    var userId: Int? = null,

    @SerializedName("joined_date")
    @Expose
    var joinedDate: String? = null,

    @SerializedName("all_ranks")
    @Expose
    var allRanks: Int? = null,

    @SerializedName("first_name")
    @Expose
    var firstName: String? = null,

    @SerializedName("image_url")
    @Expose
    var imageUrl: String? = null,

    @SerializedName("mobile")
    @Expose
    var mobile: String? = null
)


