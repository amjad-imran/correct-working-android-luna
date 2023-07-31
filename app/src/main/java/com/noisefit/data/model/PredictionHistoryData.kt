package com.noisefit.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PredictionHistoryData(
    @SerializedName("id")
    @Expose
    val id: Int? = null,
    @SerializedName("match_id")
    @Expose
    val matchId: Int? = null,
    @SerializedName("team_id")
    @Expose
    val teamId: Int? = null,
    @SerializedName("team_name")
    @Expose
    val teamName: String? = null,
    @SerializedName("date")
    @Expose
    val date: Long? = null,
    @SerializedName("points")
    @Expose
    val points: Int? = null,
    @SerializedName("image_url")
    @Expose
    val imageUrl: String? = null,
    val result: Int? = 0,//0-> Pending 1-> win 2-> lose 3-> No Result
    @SerializedName("is_collected")
    @Expose
    val isCollect: Boolean = false,


    )