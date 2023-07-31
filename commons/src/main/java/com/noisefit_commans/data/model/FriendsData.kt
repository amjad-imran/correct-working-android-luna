package com.noisefit_commans.data.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class FriendsData(
    @SerializedName("insight")
    @Expose
    val insight: String,
    val requests_count: Int? = 0,
    @SerializedName("progress")
    @Expose
    val progress: List<FriendProgress>? = null,
)

data class FriendProgress(
    @SerializedName("first_name")
    @Expose
    val firstName: String,
    @SerializedName("image_url")
    @Expose
    val imageUrl: String? = null,
    @SerializedName("calories")
    @Expose
    val calories: Long,
    @SerializedName("steps")
    @Expose
    val steps: Long,
    @SerializedName("distance")
    @Expose
    val distance: Double,
    @SerializedName("step_goal")
    @Expose
    val stepGoal: Long,
    @SerializedName("distance_goal")
    @Expose
    val distanceGoal: Double,
    @SerializedName("calories_goal")
    @Expose
    val caloriesGoal: Long,
    @SerializedName("user_emojis")
    @Expose
    var userEmoji: String? = null,
    @SerializedName("emojis")
    @Expose
    var reactions: ArrayList<Reactions>? = null,
    val user_id: Int
)

@Parcelize
data class Reactions(
    @SerializedName("emojis_type")
    @Expose
    var emoji: String? = null,
    @SerializedName("count")
    @Expose
    val count: Int? = null
) : Parcelable

@Parcelize
data class UserFriendReactions(
    @SerializedName("1")
    @Expose
    var emoji1: UserFriendListData? = null,
    @SerializedName("2")
    @Expose
    var emoji2: UserFriendListData? = null,
    @SerializedName("3")
    @Expose
    var emoji3: UserFriendListData? = null,
    @SerializedName("4")
    @Expose
    var emoji4: UserFriendListData? = null
) : Parcelable

@Parcelize
data class UserFriendListData(
    @SerializedName("count")
    @Expose
    var count: Int? = null,
    @SerializedName("users")
    @Expose
    var userFriendData: ArrayList<UserFriendData>? = null
) : Parcelable

@Parcelize
data class UserFriendData(
    @SerializedName("first_name")
    @Expose
    var firstName: String? = null,
    @SerializedName("name")
    @Expose
    var name: String? = null,
    var user_id: Long? = null,
    @SerializedName("image_url")
    @Expose
    var imageUrl: String? = null,
    @SerializedName("reaction_type")
    @Expose
    var emojiType: String? = null
) : Parcelable

@Parcelize
data class ReactionsWrapper(
    var title: String? = null,
    var count: Int? = null,
    var userFriendData: List<UserFriendData>? = null
) : Parcelable

data class ReactionsUsers(
    val count: List<ReactionModel>? = null,
    val reactions: List<UserFriendData>? = null,
    val has_next: Boolean? = null
)

data class ReactionModel(
    val reaction_type: Int,
    val count: Int
)

/*data class ReactionsCount(
    @SerializedName("1")
    @Expose
    var emojiHand: Int? = 0,
    @SerializedName("2")
    @Expose
    var emojiHeart: Int? = 0,
    @SerializedName("3")
    @Expose
    var emojiFire: Int? = 0,
    @SerializedName("4")
    @Expose
    var emoji100: Int? = 0
)*/


enum class Emoji(val emoji: String) {
    EmojiHeart("2"),
    EmojiHand("1"),
    EmojiFire("3"),
    Emoji100("4"),

}

/*
hand : 1
heart : 2
fire : 3
100 : 4
 */