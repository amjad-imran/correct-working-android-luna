package com.noisefit.data.repository.implementation

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.noisefit.BuildConfig
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.abstraction.FeedsDataSource
import com.noisefit.data.local.db.database.FeedsType
import com.noisefit.data.model.*
import com.noisefit.data.model.timeline.CommentData
import com.noisefit.data.model.timeline.FriendTimeline
import com.noisefit.data.model.timeline.ReactionData
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.source.FeedDataSource
import com.noisefit.data.repository.abstraction.FeedRepository
import com.noisefit.data.repository.pagingSource.NETWORK_PAGE_SIZE
import com.noisefit.data.repository.pagingSource.TimelinePagingSource
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionsUsers
import com.noisefit_commans.data.model.UserFriendReactions
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.DeleteCommentResponse
import com.noisefit_commans.ui.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.utils.DateFormats.checkDayDifferenceMoreOne
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URI

private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)


class FeedRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val localDataStore: DataStoredInterface,
    private val pagingSource: TimelinePagingSource,
    private val gson: Gson,
    private val feedsDBSource: FeedsDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FeedRepository {


    override fun getPagingFeedsData(): LiveData<PagingData<TimelineData>> {
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSize = 1
            ),
            pagingSourceFactory = {
                FeedDataSource(remoteDataSource)
            }, initialKey = 1
        ).liveData
    }

    override suspend fun getFriendsFriendList(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<List<FriendsFriendListData>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/friends/feed/friend"
            remoteDataSource.getFriendsFriend(url, jsonObject)
        }
    }

    override suspend fun reportFeed(
        jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<DeleteCommentResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/report"
            remoteDataSource.reportFeed(url, jsonObject)
        }
    }

    override suspend fun editComment(
        commentId: Long, jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/comment/$commentId"
            remoteDataSource.editComment(url, jsonObject)
        }
    }

    override suspend fun getComment(postId: Long): Flow<Resource<BaseApiResponse<ArrayList<CommentData>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/comment/$postId"
            remoteDataSource.getComment(url)
        }
    }

    override suspend fun addComment(
        postId: Long, jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<List<CommentData>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/comment/$postId"
            remoteDataSource.addComment(url, jsonObject)
        }
    }

    override suspend fun editReactions(
        postId: Long, reactionId: Long, jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/reaction/$postId/$reactionId"
            remoteDataSource.editReaction(url, jsonObject)
        }
    }

    override suspend fun addCommentReply(
        commentId: Long, jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/comment/reply/$commentId"
            remoteDataSource.addCommentReply(url, jsonObject)
        }
    }

    override suspend fun updateCommentReply(
        commentId: Long, commentReplyId: Long, jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/comment/reply/$commentId/$commentReplyId"
            remoteDataSource.updateCommentReply(url, jsonObject)
        }
    }

    override suspend fun getCommentReply(commentId: Long): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/comment/reply/$commentId"
            remoteDataSource.getCommentReply(url)
        }
    }

    override suspend fun deleteCommentReply(commentReplyId: Long): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/comment/reply/$commentReplyId"
            remoteDataSource.deleteCommentReply(url)
        }
    }

    override suspend fun getPostReactions(postId: Long): Flow<Resource<BaseApiResponse<List<UserFriendReactions>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/reaction/$postId"
            remoteDataSource.getPostReactions(url)
        }
    }

    override suspend fun getPostReactionsPaginate(
        postId: Long,
        getCount: Int?,
        page: Int,
        reaction: Int?
    ): Flow<Resource<BaseApiResponse<ReactionsUsers>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/feeds/reaction/v2/$postId"
            remoteDataSource.getPostReactionsPaginate(url, reaction, getCount, page)
        }
    }

    override suspend fun getPostComments(
        postId: Long,
        pageNo: Int
    ): Flow<Resource<BaseApiResponse<TimelineData>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/feeds/v2/comment/$postId"
            remoteDataSource.getPostReactionsComments(url, pageNo)
        }
    }

    override suspend fun addReaction(
        postId: Long, jsonObject: JsonObject
    ): Flow<Resource<BaseApiResponse<List<ReactionData>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/v2/reaction/$postId"
            remoteDataSource.addReaction(url, jsonObject)
        }
    }


    override suspend fun getTimeLine(jsonObject: JsonObject): Flow<PagingData<TimelineData>> {
        return Pager(config = PagingConfig(
            pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false
        ), pagingSourceFactory = {
            TimelinePagingSource(remoteDataSource, dispatcher)
        }).flow
    }

    override suspend fun getTimeLineWithoutPL(
        friendId: Long, currentPageSeries: Int
    ): Flow<Resource<BaseApiResponse<FriendTimeline>>> {
        return safeApiCallFlow(dispatcher) {
            var url = "${BuildConfig.BASE_URL_NEW}/feeds/user/timeline?page=$currentPageSeries"
            if (friendId != -1L) {
                url += "&friend_id=${friendId}"
            }
            remoteDataSource.getTimeLine(url)
        }
    }

    override suspend fun getDashboardFeed(
        forceRefresh: Boolean,
        page: Int
    ): Flow<Resource<BaseApiResponse<FeedResponse>>> {
        return flow {
            val resultData = ArrayList<TimelineData>()
            var shouldAppendData = false
            var pageToFetch = page

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                if (forceRefresh) {
                    localDataStore.setTimeLineCurrentPageCount(1)
                    feedsDBSource.removeTimelineFeeds()
                    pageToFetch = 1
                    return@safeCacheCall null
                }
                val lastPageCount = localDataStore.getTimeLineCurrentPageCount()

                if (page > lastPageCount) {
                    shouldAppendData = true
                }

                val localData =
                    feedsDBSource.getTimelineFeeds()

                if (localData == null) {
                    pageToFetch = 1
                    return@safeCacheCall null
                }

                val lastCallTime = localData.first().getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne() || forceRefresh || lastCallTime.checkDayDifferenceMoreNMinutes(
                        FRIENDS_AUTO_REFRESH_MINUTES
                    )

                if (shouldCallApi) {
                    pageToFetch = 1
                    feedsDBSource.removeTimelineFeeds()
                    localDataStore.setTimeLineCurrentPageCount(1)
                    resultData.clear()
                    return@safeCacheCall null
                } else {

                    if (localData.isEmpty()) {
                        pageToFetch = 1
                        return@safeCacheCall null
                    }

                    val feedsList = ArrayList<TimelineData>()
                    localData.forEach {

                        it.value?.let { data ->
                            val parsedData = Gson().fromJson<TimelineData>(
                                data
                            )
                            feedsList.add(parsedData)
                        }

                    }

                    return@safeCacheCall feedsList
                }
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData.addAll(it)
                        }
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }


            if (resultData.isNotEmpty() && !shouldAppendData) {
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = FeedResponse(
                                response = resultData,
                                has_next = true
                            ),
                            message = "",
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url =
                    "${BuildConfig.BASE_URL_NEW}/feeds/user/dashboard/timeline?page=$pageToFetch"
                remoteDataSource.getDashboardFeed(url)
            }


            var hasNext = false
            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }

                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }

                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }

                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            hasNext = response.has_next
                            if ((response.response?.size ?: 0) > 0) {
                                localDataStore.setTimeLineCurrentPageCount(pageToFetch)
                            }

                            if (resultData.isEmpty()) {
                                resultData.addAll(response.response.apply {
                                    this?.forEach {
                                        if ((it.feedType ?: "").equals("ad", true)) {
                                            it.postId = (it.actionId * -1L)
                                        }
                                    }
                                } ?: ArrayList())
                            } else {

                                response.response.apply {
                                    this?.forEach { sData ->
                                        if ((sData.feedType ?: "").equals("ad", true)) {
                                            sData.postId = (sData.actionId * -1L)
                                        }

                                        val index = resultData.indexOfFirst {
                                            it.postId == sData.postId
                                        }
                                        if (index == -1) {
                                            resultData.add(sData)
                                        } else {
                                            resultData.removeAt(index)
                                            resultData.add(index, sData)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (resultData.isNotEmpty()) {
                safeCacheCall(Dispatchers.IO) {

                    feedsDBSource.removeTimelineFeeds()
                    resultData.forEach {
                        feedsDBSource.insertData(
                            FeedsDbValue(
                                key = it.postId.toString(),
                                value = gson.toJson(it),
                                type = FeedsType.TIMELINE.name
                            )
                        )
                    }
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
                                        data = FeedResponse(
                                            response = resultData,
                                            has_next = hasNext
                                        ),
                                        message = "",
                                    )
                                )
                            )
                        }

                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
    }

    override suspend fun updateOfflineCommentsData(
        postId: Long?,
        it: List<CommentData>?,
        commentsCount: Int
    ) {

        if (postId == null) return
        if (it == null) return

        CoroutineScope(Dispatchers.IO).launch {
            val post = feedsDBSource.getTimelineFeedById(postId) ?: return@launch
            val postObj = Gson().fromJson<TimelineData>(
                post.value!!
            )
            postObj.commentsCount = commentsCount
            postObj.comment = if (it.isEmpty()) null else it.first()

            feedsDBSource.updateFeedById(postId, gson.toJson(postObj))
        }
    }

    override suspend fun removeOfflineFeedData() {
        CoroutineScope(Dispatchers.IO).launch {
            feedsDBSource.removeTimelineFeeds()
        }
    }

    override suspend fun updateOfflineReactionData(
        postId: Long?,
        emoji: Emoji?,
        reactionList: List<ReactionData>
    ) {
        if (postId == null) return

        CoroutineScope(Dispatchers.IO).launch {
            val post = feedsDBSource.getTimelineFeedById(postId) ?: return@launch
            val postObj = Gson().fromJson<TimelineData>(
                post.value!!
            )
            if (emoji != null) {
                postObj.userReaction = emoji.emoji
            } else {
                postObj.userReaction = null

            }
            var likeCount = 0
            reactionList.forEach {
                likeCount += it.count
            }
            postObj.likesCount = likeCount
            postObj.reaction = reactionList
            feedsDBSource.updateFeedById(postId, gson.toJson(postObj))
        }


    }

    override suspend fun deletePOrC(
        postId: Long, commentId: Long?
    ): Flow<Resource<BaseApiResponse<DeleteCommentResponse>>> {
        return safeApiCallFlow(dispatcher) {
            var url = "${BuildConfig.BASE_URL_NEW}/feeds/post/${postId}"

            if (commentId != -1L) {
                url = "${BuildConfig.BASE_URL_NEW}/feeds/comment/$commentId"
            } else {
                feedsDBSource.removeDataByKey(postId.toString(), FeedsType.TIMELINE)
            }

            remoteDataSource.deletePorC(url)
        }
    }

    override suspend fun getPostList(userId: Long): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/user/post_count/$userId"
            remoteDataSource.getPostList(url)
        }
    }

    override suspend fun getReportedList(): Flow<Resource<BaseApiResponse<List<ReportAbuseData>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/report_list"
            remoteDataSource.getReportedList(url)
        }
    }

    override suspend fun getImageTemplates(): Flow<Resource<BaseApiResponse<List<ImageTemplate>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/template/list"
            remoteDataSource.getImageTemplates(url)
        }
    }

    override suspend fun getTagFriendsList(): Flow<Resource<BaseApiResponse<List<MentionUser>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/friends/feed/tag/friend_list"
            remoteDataSource.getTagFriendsList(url)
        }
    }

    override suspend fun updatePost(
        caption: String,
        postId: Long,
        mappedUser: List<MentionUser>
    ): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/post/$postId"

            val mentionsArray = JsonArray()
            mappedUser.forEach {
                val userObj = JsonObject().apply {
                    addProperty("id", it.id)
                    addProperty("first_name", it.first_name)
                    addProperty("start_pos", it.start_pos)
                }
                mentionsArray.add(userObj)
            }

            val requestObject = JsonObject().apply {
                this.addProperty("caption", caption)
                this.add("tagged_user", mentionsArray)
            }

            feedsDBSource.removeTimelineFeeds()
            localDataStore.setTimeLineCurrentPageCount(1)

            remoteDataSource.updatePost(url, requestObject)
        }
    }

    override suspend fun createPost(
        imageUri: URI?, caption: String, mappedUser: List<MentionUser>
    ): Flow<Resource<BaseApiResponse<Any>>> {
        var mediaType: RequestBody? = null

        val requestFile = if (imageUri != null) {
            mediaType = RequestBody.create("text/plain".toMediaTypeOrNull(), "image")
            val file = File(imageUri.path)
            MultipartBody.Part.createFormData(
                "files", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        } else {
            null
        }


        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/post"
            val usernameBody: RequestBody =
                RequestBody.create("text/plain".toMediaTypeOrNull(), caption)
            val mappings: RequestBody =
                RequestBody.create("text/plain".toMediaTypeOrNull(), gson.toJson(mappedUser))

            feedsDBSource.removeTimelineFeeds()
            localDataStore.setTimeLineCurrentPageCount(1)

            remoteDataSource.createPost(url, requestFile, usernameBody, mappings, mediaType)
        }
    }

    override suspend fun getTemplateList(): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/template/list"
            remoteDataSource.getTemplateList(url)

        }
    }

    override suspend fun getNoiseProfileData(): Flow<Resource<BaseApiResponse<NoiseProfileData>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/profile/noise"
            remoteDataSource.getNoiseProfileData(url)
        }
    }


    override suspend fun getAdminTimeline(
        currentPageSeries: Int
    ): Flow<Resource<BaseApiResponse<FriendTimeline>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/feeds/noise_admin/profile_timeline?page=$currentPageSeries"
            remoteDataSource.getNoiseTimeLine(url)
        }
    }

    override suspend fun getPostDetailsData(postId: Long): Flow<Resource<BaseApiResponse<TimelineData>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/feeds/user/post/detail/${postId}"
            remoteDataSource.getPostDetailsData(url)
        }
    }

}