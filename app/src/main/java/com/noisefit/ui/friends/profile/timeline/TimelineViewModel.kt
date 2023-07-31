package com.noisefit.ui.friends.profile.timeline


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.handleReactionData
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.model.timeline.ReactionData
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FeedRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TimelineViewModel
@Inject constructor(
    private val feedRepository: FeedRepository,
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {

    private val _userFriendReactions = MutableLiveData<Event<ArrayList<ReactionsWrapper>>>()
    val userFriendReactions: LiveData<Event<ArrayList<ReactionsWrapper>>>
        get() = _userFriendReactions

    private var _emojiUpdatePostAt =
        MutableLiveData<Event<Triple<Emoji?, Int, List<ReactionData>>>>()
    var emojiUpdatePostAt: LiveData<Event<Triple<Emoji?, Int, List<ReactionData>>>> =
        _emojiUpdatePostAt

    private val _loadingMore = MutableLiveData<Boolean>()
    fun getLoadMoreLoading(): LiveData<Boolean> = _loadingMore

    private val _loadingInitial = MutableLiveData<Boolean>()
    fun getInitialLoading(): LiveData<Boolean> = _loadingInitial

    var friendId: Long = -1L


    var lastClickedPost: TimelineData? = null

    var editPost = MutableLiveData<Event<TimelineData?>>()
    var deletePost = MutableLiveData<Event<Long?>>()


    private var _timeLineList = MutableLiveData<List<TimelineData>>()
    var timeLineList: LiveData<List<TimelineData>> = _timeLineList

    private var _newPageData = MutableLiveData<List<TimelineData>>()
    var newPageData: LiveData<List<TimelineData>> = _newPageData

    private var _updatePostAt = MutableLiveData<Event<Int>>()
    var updatePostAt: LiveData<Event<Int>> = _updatePostAt


    var timelinePageLimit = -1
    var lastDataSize = -1
    var currentPageSeries = 1
    var isFeedApiLoading = false


    //    val concertList: MutableLiveData<PagedList<TimelineData>> =
//        _timeLineList.(pageSize = 50)
    init {

    }


//     fun getUserTimeLine() {
//        val jsonObject = JsonObject()
//        if (friendId != -1) {
//            jsonObject.addProperty("friend_id", friendId)
//        }
//        return feedRepository.getTimeLine(jsonObject)
//            .map { pagingData ->
//                pagingData.map { timelineData ->
////                    mapper.mapDomainMovieToUi(domainMovie = it)
//                    return@map timelineData
//                }
//            }
//            .cachedIn(viewModelScope)
//    }

    fun postEmoji(emoji: Emoji?, postId: Long, position: Int) {

        val jsonObject = JsonObject()
        emoji?.let {
            jsonObject.addProperty("reaction", emoji.emoji)
        }
        viewModelScope.launch {
            feedRepository.addReaction(postId, jsonObject).collect { resource ->
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
                                    postEmoji(emoji, postId, position)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _emojiUpdatePostAt.postValue(Event(Triple(emoji, position, it)))
                            //handlePostEmoji(emoji, position, it)
                        }
                    }
                }
            }
        }

    }

    fun getPostReactions(postId: Long) {
        viewModelScope.launch {
            feedRepository.getPostReactions(postId)
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
                                        getPostReactions(postId)
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

    private fun handlePostEmoji(
        emoji: Emoji?,
        position: Int,
        timeLineData: TimelineData,
        reactionList: List<ReactionData>
    ) {
        _timeLineList.value?.let { timeLineList ->
            if (emoji != null) {
                timeLineData.userReaction = emoji.emoji
            } else {
                timeLineData.userReaction = null

            }
            var likeCount = 0
            reactionList.forEach {
                likeCount += it.count
            }
            timeLineData.likesCount = likeCount

            timeLineList[position].reaction = reactionList
            _updatePostAt.postValue(Event(position))
        }
    }

    fun getUserTimeLineWithoutPl() {
        if (timelinePageLimit != -1 && lastDataSize != -1) {
            if (lastDataSize < timelinePageLimit) {
                LOGS.d("Returning from getUserTimeLineWithoutPl()")
                return
            }
        }
        isFeedApiLoading = true

        viewModelScope.launch {
            feedRepository.getTimeLineWithoutPL(friendId, currentPageSeries).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        isFeedApiLoading = false
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        if (currentPageSeries == 1) {
                            _loadingInitial.postValue(resource.loading)
                        } else {
                            _loadingMore.postValue(resource.loading)
                        }
                    }
                    is Resource.NetworkError -> {
                        isFeedApiLoading = false
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getUserTimeLineWithoutPl()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        isFeedApiLoading = false
                        resource.data?.data?.let {
                            it.response?.let { data ->
                                if (currentPageSeries == 1) {
                                    _timeLineList.postValue(data)
                                } else {
                                    _newPageData.postValue(data)
                                }
                            }
                            timelinePageLimit = it.limit ?: -1
                            lastDataSize = it.response?.size ?: 0
                            currentPageSeries++
                        }
                    }
                }
            }
        }

    }

    fun isMyProfile(): Boolean {
        return friendId < 0
    }


}