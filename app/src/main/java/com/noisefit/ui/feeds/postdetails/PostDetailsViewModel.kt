package com.noisefit.ui.feeds.postdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.handleReactionData
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.db.fromJson
import com.noisefit.data.model.timeline.CommentData
import com.noisefit.data.model.timeline.ReactionData
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FeedRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit.util.formatPostLeadingString
import com.noisefit.util.formatPostTrailingString
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit_commans.data.model.User
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val feedRepository: FeedRepository
) : BaseViewModel() {
    val navigateUp = MutableLiveData<Event<Boolean>>()
    private var _postDetailsData = MutableLiveData<TimelineData>()
    var postDetailsData: LiveData<TimelineData> = _postDetailsData

    private var _emojiUpdatePostAt =
        MutableLiveData<Event<Pair<Emoji?, List<ReactionData>>>>()
    var emojiUpdatePostAt: LiveData<Event<Pair<Emoji?, List<ReactionData>>>> =
        _emojiUpdatePostAt

    private val _userFriendReactions = MutableLiveData<Event<ArrayList<ReactionsWrapper>>>()
    val userFriendReactions: LiveData<Event<ArrayList<ReactionsWrapper>>>
        get() = _userFriendReactions


    private var _userCommentList = MutableLiveData<ArrayList<CommentData>>()
    var userCommentList: LiveData<ArrayList<CommentData>> = _userCommentList


    private var _commentPosted = MutableLiveData<Event<Boolean>>()
    var commentPosted: LiveData<Event<Boolean>> = _commentPosted


    var showDeleteBottomSheet = MutableLiveData<Event<Boolean>>()


    var editPost = MutableLiveData<Event<TimelineData?>>()
    var deletePost = MutableLiveData<Event<Long?>>()
    var clickedPost: TimelineData? = null

    var postId: Long = -1L
    var userId: Long = -1L
    var user: User? = null
    var commentId: Long? = null

    var hasNext: Boolean? = null
    var commentPageNo = 0
    var isCommentsApiLoading = false
    var isMyPost = false

    private val _loadingMore = MutableLiveData<Boolean>()
    fun getLoadMoreLoading(): LiveData<Boolean> = _loadingMore

    init {
        user = localDataStore.getUser()
        userId = localDataStore.getUser()?.id?.toLong() ?: -1L
    }

    fun updateDeleteBottomSheet(status: Boolean) {
        showDeleteBottomSheet.postValue(Event(status))
    }

    fun postEmoji(emoji: Emoji?) {

        val jsonObject = JsonObject()
        emoji?.let {
            jsonObject.addProperty("reaction", emoji.emoji)
        }
        viewModelScope.launch {
            postId?.let {
                feedRepository.addReaction(it, jsonObject).collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }

                        is Resource.Loading -> {
                            setLoading(resource.loading)
                        }

                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                this.uiComponentType as UIComponentType.RetryApiDialog
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        postEmoji(emoji)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data?.let {
                                _emojiUpdatePostAt.postValue(Event(Pair(emoji, it)))
                                feedRepository.updateOfflineReactionData(postId, emoji, it)

                            }
                        }
                    }
                }
            }
        }

    }

    fun getPostDetailsData() {
        hasNext = null
        commentPageNo = 0
        viewModelScope.launch {
            postId.let {
                feedRepository.getPostDetailsData(it)
                    .collect { resource ->
                        when (resource) {
                            is Resource.GenericError -> {
                                sendMessage(resource.message)
                                navigateUp.postValue(Event(true))
                            }

                            is Resource.Loading -> {
                                setLoading(resource.loading)
                            }

                            is Resource.NetworkError -> {
                                setApiErrors(resource.response.apply {
                                    this.uiComponentType as UIComponentType.RetryApiDialog
                                    (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                        override fun yes() {
                                            getPostDetailsData()
                                        }

                                        override fun no() {}
                                    }
                                })
                            }
                            is Resource.Success -> {
                                resource.data?.data?.let { it ->
                                    hasNext = it.has_next
                                    commentPageNo += 1
                                    isMyPost=it.isMyPost
                                    _userCommentList.postValue(
                                        (it.commentsList ?: ArrayList()) as ArrayList<CommentData>
                                    )

                                    feedRepository.updateOfflineCommentsData(
                                        postId,
                                        it.commentsList,
                                        it.commentsCount
                                    )
                                    _postDetailsData.postValue(it)
                                }
                            }
                        }
                    }
            }
        }
    }

    fun getPostReactions() {
        viewModelScope.launch {
            postId?.let {
                feedRepository.getPostReactions(it)
                    .collect { resource ->
                        when (resource) {
                            is Resource.GenericError -> {
                                sendMessage(resource.message)
                            }

                            is Resource.Loading -> {
                                setLoading(resource.loading)
                            }

                            is Resource.NetworkError -> {
                                setApiErrors(resource.response.apply {
                                    this.uiComponentType as UIComponentType.RetryApiDialog
                                    (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                        override fun yes() {
                                            getPostReactions()
                                        }

                                        override fun no() {}
                                    }
                                })
                            }

                            is Resource.Success -> {
                                resource.data?.data?.let { it ->
                                    if (it.isNotEmpty()) {
                                        _userFriendReactions.postValue(
                                            Event(
                                                it.first().handleReactionData()
                                            )
                                        )
                                    } else {
                                        _userFriendReactions.postValue(Event(ArrayList()))
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    fun addComment(comment: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(
            "comment",
            comment.formatPostLeadingString().formatPostTrailingString()
        )
        viewModelScope.launch {
            feedRepository.addComment(postId, jsonObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    addComment(comment)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (it.isNotEmpty()) {
                                var commentsCount = 0
                                if (_userCommentList.value.isNullOrEmpty()) {
                                    _userCommentList.postValue(ArrayList<CommentData>().apply {
                                        add(it[0])
                                    })
                                    commentsCount = 1
                                } else {
                                    ((_userCommentList.value) as ArrayList).add(0, it[0])
                                    _userCommentList.postValue(_userCommentList.value)
                                    commentsCount = try {
                                        it[1].commentsCount
                                    } catch (exp: Exception) {
                                        _userCommentList.value?.size ?: 0
                                    }
                                }
                                _commentPosted.postValue(Event(true))

                                feedRepository.updateOfflineCommentsData(
                                    postId,
                                    _userCommentList.value,
                                    commentsCount
                                )
                            }


                        }
                    }
                }
            }
        }
    }

    fun removeComment(commentId: Long?, commentCount: Int) {
        if (commentId == null) return

        val lastValue = _userCommentList.value
        val index = lastValue?.indexOfFirst {
            it.commentId == commentId
        }

        tryCatch {
            if (index != null && index != -1) {
                (lastValue as ArrayList<CommentData>).removeAt(index)
                lastValue.let {
                    _userCommentList.postValue(it)

                    viewModelScope.launch {
                        feedRepository.updateOfflineCommentsData(
                            postId,
                            _userCommentList.value,
                            commentCount
                        )
                    }
                }
            }
        }
    }

    fun getComments() {
        LOGS.i("getComments API CALLED")
        if (hasNext != null && hasNext == false) {
            return
        }

        isCommentsApiLoading = true


        viewModelScope.launch {
            feedRepository.getPostComments(postId, commentPageNo)
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            isCommentsApiLoading = false
                            sendMessage(resource.message)
                        }

                        is Resource.Loading -> {

                            _loadingMore.postValue(resource.loading)
                        }

                        is Resource.NetworkError -> {
                            isCommentsApiLoading = false
                            setApiErrors(resource.response.apply {
                                this.uiComponentType as UIComponentType.RetryApiDialog
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getComments()
                                    }

                                    override fun no() {}
                                }
                            })
                        }

                        is Resource.Success -> {
                            isCommentsApiLoading = false

                            resource.data?.data?.let {
                                hasNext = it.has_next
                                commentPageNo += 1

                                val lastData = _userCommentList.value ?: ArrayList()
                                lastData.addAll(
                                    (it.commentsList ?: ArrayList()) as ArrayList<CommentData>
                                )
                                _userCommentList.postValue(
                                    lastData
                                )
                            }
                        }
                    }
                }
        }
    }
}