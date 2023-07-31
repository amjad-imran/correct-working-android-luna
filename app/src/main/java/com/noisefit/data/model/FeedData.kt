package com.noisefit.data.model

import com.google.gson.annotations.SerializedName
import com.noisefit.data.model.timeline.CommentData
import com.noisefit.data.model.timeline.ReactionData
import com.noisefit.data.model.timeline.TimelineData


data class FeedResponse(
    @SerializedName("response")
    var response: List<TimelineData>? = null,
    val limit: Int? = null,
    val has_next: Boolean = true,
    val requests_count: Int = 0
)

data class FeedData(
    @SerializedName("feed_type")
    var feedType: String? = null,
    @SerializedName("post_id")
    var postId: Long,
    @SerializedName("user_id")
    var userId: Long,
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("caption")
    var caption: String? = null,
    @SerializedName("location")
    var location: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("comments_count")
    var commentsCount: Long = 0,
    @SerializedName("likes_count")
    var likesCount: Long = 0,
    @SerializedName("media_url")
    var mediaUrl: List<String>? = null,
    @SerializedName("image_url")
    var imageUrl: String? = null,
    @SerializedName("user_type")
    var userType: String? = null,
    @SerializedName("user_reaction")
    var userReaction: String? = null,

    @SerializedName("action_id")
    var actionId: Int = -1,
    @SerializedName("action_url")
    var actionUrl: String? = null,

    @SerializedName("comment")
    var comment: CommentData? = null,
    @SerializedName("reaction")
    var reaction: List<ReactionData>? = null,


    )

data class ADSData(
    @SerializedName("feed_type")
    var feedType: String? = null,
    @SerializedName("media_url")
    var mediaUrl: List<String>? = null,
    @SerializedName("action_id")
    var actionId: Int = 0,
    @SerializedName("action_url")
    var actionUrl: String? = null,
)
