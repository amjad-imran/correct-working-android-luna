package com.noisefit.data.repository.abstraction

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.google.gson.JsonObject
import com.noisefit.data.model.*
import com.noisefit.data.model.timeline.CommentData
import com.noisefit.data.model.timeline.FriendTimeline
import com.noisefit.data.model.timeline.ReactionData
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionsUsers
import com.noisefit_commans.data.model.UserFriendReactions
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.DeleteCommentResponse
import kotlinx.coroutines.flow.Flow
import java.net.URI

interface FeedRepository {
    fun getPagingFeedsData(): LiveData<PagingData<TimelineData>>
    suspend fun getFriendsFriendList(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<List<FriendsFriendListData>>>>
    suspend fun getTimeLineWithoutPL(
        friendId: Long,
        currentPageSeries: Int
    ): Flow<Resource<BaseApiResponse<FriendTimeline>>>

    suspend fun getAdminTimeline(
        currentPageSeries: Int
    ): Flow<Resource<BaseApiResponse<FriendTimeline>>>

    suspend fun getPostReactions(postId: Long): Flow<Resource<BaseApiResponse<List<UserFriendReactions>>>>

    suspend fun getPostReactionsPaginate(
        postId: Long, getCount: Int?,
        page: Int, reaction: Int? = null
    ): Flow<Resource<BaseApiResponse<ReactionsUsers>>>

    suspend fun getPostComments(
        postId: Long,
        pageNo: Int
    ): Flow<Resource<BaseApiResponse<TimelineData>>>

    suspend fun getDashboardFeed(
        forceRefresh: Boolean,
        page: Int
    ): Flow<Resource<BaseApiResponse<FeedResponse>>>

    suspend fun getTimeLine(jsonObject: JsonObject): Flow<PagingData<TimelineData>>
    suspend fun deletePOrC(
        postId: Long,
        commentId: Long?
    ): Flow<Resource<BaseApiResponse<DeleteCommentResponse>>>

    suspend fun reportFeed(
        jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<DeleteCommentResponse>>>

    suspend fun editComment(
        commentId: Long,
        jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getComment(postId: Long): Flow<Resource<BaseApiResponse<ArrayList<CommentData>>>>
    suspend fun addComment(
        postId: Long,
        jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<List<CommentData>>>>

    suspend fun editReactions(
        postId: Long,
        reactionId: Long,
        jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun addCommentReply(
        commentId: Long,
        jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun updateCommentReply(
        commentId: Long,
        commentReplyId: Long,
        jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getCommentReply(commentId: Long): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun deleteCommentReply(commentReplyId: Long): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun addReaction(
        postId: Long,
        jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<List<ReactionData>>>>

    suspend fun getTemplateList(): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getPostList(userId: Long): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun getReportedList(): Flow<Resource<BaseApiResponse<List<ReportAbuseData>>>>

    suspend fun getImageTemplates(): Flow<Resource<BaseApiResponse<List<ImageTemplate>>>>

    suspend fun getTagFriendsList(): Flow<Resource<BaseApiResponse<List<MentionUser>>>>

    suspend fun createPost(
        imageUri: URI?,
        caption: String,
        mappedUser: List<MentionUser>
    ): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun updatePost(
        caption: String,
        postId: Long,
        mappedUser: List<MentionUser>
    ): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getNoiseProfileData(): Flow<Resource<BaseApiResponse<NoiseProfileData>>>

    suspend fun updateOfflineCommentsData(postId: Long?, it: List<CommentData>?, commentsCount: Int)

    suspend fun updateOfflineReactionData(
        postId: Long?,
        emoji: Emoji?,
        reactionList: List<ReactionData>
    )

    suspend fun removeOfflineFeedData()

    suspend fun getPostDetailsData(postId: Long): Flow<Resource<BaseApiResponse<TimelineData>>>

}