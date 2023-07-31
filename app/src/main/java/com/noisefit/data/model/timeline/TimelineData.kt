package com.noisefit.data.model.timeline

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.noisefit.data.model.MentionUser
import kotlinx.parcelize.Parcelize

data class FriendTimeline(
    @SerializedName("response") var response: List<TimelineData>? = null,
    @SerializedName("profile_type") var profileType: String? = null,
    val limit: Int? = null,
    @SerializedName("is_profile_acessible") var isProfileAcessible: String? = null,
)

@Parcelize
data class TimelineData(
    var expandedState: Boolean = false,
    @SerializedName("feed_type") var feedType: String? = null,

    @SerializedName("post_id") var postId: Long,

    @SerializedName("action_id") var actionId: Int,

    @SerializedName("user_id") var userId: Long,

    @SerializedName("created_at") var createdAt: String? = null,

    @SerializedName("caption") var caption: String? = null,

    @SerializedName("tagged_user") var taggedUser: List<MentionUser>? = null,

    @SerializedName("location") var location: String? = null,

    @SerializedName("name") var name: String? = null,

    @SerializedName("comments_count") var commentsCount: Int = 0,

    @SerializedName("likes_count") var likesCount: Int = 0,

    @SerializedName("media_url") var mediaUrl: List<String>? = null,

    @SerializedName("image_url") var imageUrl: String? = null,

    @SerializedName("user_type") var userType: String? = null,

    @SerializedName("media_type") var mediaType: String? = null,

    @SerializedName("user_reaction") var userReaction: String? = null,

    @SerializedName("comment") var comment: CommentData? = null,
    @SerializedName("is_my_post") var isMyPost: Boolean = false,

    @SerializedName("comments") var commentsList: List<CommentData>? = null,
    var has_next: Boolean? = false,
    @SerializedName("reaction") var reaction: List<ReactionData>? = null,
    var like: LikeData? = null,
) : Parcelable

@Parcelize
data class ReactionData(
    @SerializedName("reaction_type") var reactionType: String? = null,
    @SerializedName("count") var count: Int = 0,
) : Parcelable

@Parcelize
data class LikeData(
    @SerializedName("image_url") var imageUrl: String? = null,
    var name: String? = null,
    var count: Int = 0,
) : Parcelable

@Parcelize
data class CommentData(
    @SerializedName("comment_count") var commentsCount: Int = 0,
    @SerializedName("comment") var comment: String? = null,
    @SerializedName("comment_id") var commentId: Long = 0,
    @SerializedName("post_id") var postId: Long = 0,
    @SerializedName("user_id") var userId: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("image_url") var imageUrl: String? = null,
    @SerializedName("is_my_comment") var isMyComment: Boolean = false,
    @SerializedName("user_type") var userType: String? = null
) : Parcelable