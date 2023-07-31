package com.noisefit.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.data.response.NplLeague
import com.noisefit_commans.data.response.PrizeInfo

data class ScoreCardData(
    @SerializedName("score_card")
    @Expose
    val scoreCard: ScoreData? = null,
    @SerializedName("prize_info")
    @Expose
    val prizeInfo: PrizeInfo? = null,
    @SerializedName("banners")
    @Expose
    val banners: List<String>? = null,
    @SerializedName("quiz")
    @Expose
    val quiz: Quiz? = null,
    @SerializedName("npl_league")
    @Expose
    val nplLeague: NplLeague? = null,
    @SerializedName("banners_fame")
    @Expose
    val bannersFame: List<String>? = null,
    @SerializedName("watchface")
    @Expose
    val watchFace: WatchFaces? = null,
)

data class WatchFaces(
    val id: Int? = null,
    val name: String? = null,
    val faces: List<Faces>? = null,
    val totalCount: Int? = null

)

data class Faces(
    @SerializedName("watchface_id")
    val watchfaceId: Long? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("category_id")
    val categoryId: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("downloads")
    val downloads: Long? = null,
    @SerializedName("image_type")
    val imageType: String? = null,
    @SerializedName("is_favourite")
    val isFavourite: String? = null,
)

data class ScoreData(
    @SerializedName("user_wins")
    @Expose
    val userWins: Int? = 0,
    @SerializedName("user_losses")
    @Expose
    val userLosses: Int? = 0,
    @SerializedName("matches_left")
    @Expose
    val matchesLeft: Int? = 0
)

data class Quiz(
    @SerializedName("total_participants")
    val totalParticipants: Long,
    @SerializedName("total_coins")
    val total_coins: Long,
    @SerializedName("elapsed_time")
    val elapsedTime: Long? = null,
)

