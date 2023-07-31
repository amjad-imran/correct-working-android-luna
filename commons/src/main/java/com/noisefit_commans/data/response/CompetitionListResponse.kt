package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class CompetitionListResponse(
    @SerializedName("competitions")
    var competitions: List<Competitions>? = null,
    @SerializedName("current_time")
    var currentTime: String? = null
)

data class Competitions(
    @SerializedName("comp_id")
    var compId: Int? = null,
    @SerializedName("user_name")
    var userName: String? = null,
    @SerializedName("friend_name")
    var friendName: String? = null,
    @SerializedName("user_image")
    var userImage: String? = null,
    @SerializedName("friend_image")
    var friendImage: String? = null,
    @SerializedName("status")
    var status: String? = null,
    @SerializedName("is_winner")
    var isWinner: Boolean = false,
    @SerializedName("user_progress")
    var userProgress: Long? = null,
    @SerializedName("friend_progress")
    var friendProgress: Long? = null,
    @SerializedName("goal")
    var goal: Long? = null,
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("start_at")
    var startAt: String? = null,
    @SerializedName("end_at")
    var endAt: String? = null,

    )