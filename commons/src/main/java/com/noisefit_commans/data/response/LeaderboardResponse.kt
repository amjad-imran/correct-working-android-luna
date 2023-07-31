package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class LeaderboardResponse(
    @SerializedName("leadership") val leadership : ArrayList<Leadership>? = null,
    @SerializedName("buddiesLeadership") val buddyLeadership : ArrayList<Leadership>? = null

)

data class Leadership (
    @SerializedName("progress") val progress : Double? = null,
    @SerializedName("user_id") val user_id : Int,
    @SerializedName("joined_date") val joined_date : String? = null,
    @SerializedName("user_rank") val user_rank : Int? = 0,
    @SerializedName("first_name") val first_name : String,
    @SerializedName("image_url") val image_url : String? = null,
    @SerializedName("mobile") val mobile : String ? = null,
    @SerializedName("status") var status : String ? = null,
    @SerializedName("team_name") val teamName : String ? = null,
    @SerializedName("my_rank") val myRank : Int? = null

)