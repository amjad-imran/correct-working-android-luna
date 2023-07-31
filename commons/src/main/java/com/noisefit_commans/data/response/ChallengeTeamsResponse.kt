package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class ChallengeTeamsResponse(
    @SerializedName("team_leadership") val team_leadership : ArrayList<TeamLeadership>
)

data class TeamLeadership (
    @SerializedName("progress") val progress : Double? = null,
    @SerializedName("team_id") val team_id : Int? = null,
    @SerializedName("all_ranks") val all_ranks : Int? = null,
    @SerializedName("id") val id : Int? = null,
    @SerializedName("activity_challenge_id") val activity_challenge_id : Int? = null,
    @SerializedName("team_name") val team_name : String? = null,
    @SerializedName("image_url") val image_url : String? = null,
    @SerializedName("created_at") val created_at : String? = null,
    @SerializedName("updated_at") val updated_at : String? = null,
    @SerializedName("participants") val participants : String? = null
)