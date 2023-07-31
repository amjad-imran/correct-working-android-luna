package com.noisefit.ui.feeds.feed

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.gson.JsonObject
import com.noisefit.data.handleReactionData
import com.noisefit.data.model.ADSData
import com.noisefit.data.model.FeedOverView
import com.noisefit.data.model.timeline.ReactionData
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FeedRepository
import com.noisefit.session.SessionManager
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel
@Inject
constructor(
    val feedRepository: FeedRepository,
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK,

    ) : BaseViewModel() {

    private var _emojiUpdatePostAt =
        MutableLiveData<Event<Triple<Emoji?, Int, List<ReactionData>>>>()
    var emojiUpdatePostAt: LiveData<Event<Triple<Emoji?, Int, List<ReactionData>>>> =
        _emojiUpdatePostAt

    private val _userFriendReactions = MutableLiveData<Event<ArrayList<ReactionsWrapper>>>()
    val userFriendReactions: LiveData<Event<ArrayList<ReactionsWrapper>>>
        get() = _userFriendReactions

    private val _loadingMore = MutableLiveData<Boolean>()
    fun getLoadMoreLoading(): LiveData<Boolean> = _loadingMore

    private val _loadingInitial = MutableLiveData<Boolean>()
    fun getInitialLoading(): LiveData<Boolean> = _loadingInitial


    var userId: Long = -1L
    private var _feedOverviewList = MutableLiveData<List<TimelineData>>()
    var feedOverviewList: LiveData<List<TimelineData>> = _feedOverviewList

    private var _feedOverviewListAdd = MutableLiveData<Event<List<FeedOverView>>>()
    var feedOverviewListAdd: LiveData<Event<List<FeedOverView>>> = _feedOverviewListAdd

    var editPost = MutableLiveData<Event<TimelineData?>>()
    var deletePost = MutableLiveData<Event<Long?>>()
    var lastClickedPost: TimelineData? = null

    var hasNextData = true
    var currentPageSeries = 1
    var isFeedApiLoading = false

    var requestCount = MutableLiveData<Int>()


    init {
        userId = localDataStore.getUser()?.id?.toLong() ?: -1L
        currentPageSeries = localDataStore.getTimeLineCurrentPageCount()
    }

    fun resetPaginationData() {
        hasNextData = true
        currentPageSeries = localDataStore.getTimeLineCurrentPageCount()
    }

    fun getDashboardFeed(forceRefresh: Boolean) {

        if (!hasNextData) return
        isFeedApiLoading = true

        if (forceRefresh) {
            _feedOverviewList.value = ArrayList()
        }


        viewModelScope.launch {
            feedRepository.getDashboardFeed(forceRefresh, currentPageSeries).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        isFeedApiLoading = false
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        if (feedOverviewList.value.isNullOrEmpty() || forceRefresh) {
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
                                    getDashboardFeed(forceRefresh)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        isFeedApiLoading = false
                        resource.data?.data?.let {

                            it.response?.let { data ->
                                _feedOverviewList.postValue(it.response ?: ArrayList())
                            }
                            hasNextData = it.has_next
                            //requestCount.postValue(it.requests_count)

                            currentPageSeries = (localDataStore.getTimeLineCurrentPageCount() + 1)

                        }
                    }
                }
            }
        }
    }


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
                            feedRepository.updateOfflineReactionData(postId, emoji, it)

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

    fun clearFeedData() {
        _feedOverviewList.value = ArrayList()
    }


    /*  private fun handleFeedData(feedList: List<TimelineData>, add: Boolean) {
          val tempFeedList = ArrayList<FeedOverView>()
          feedList.forEach { feedData ->
              when (feedData.feedType?.lowercase()) {
                  "post" -> {
                      tempFeedList.add(FeedOverView.Post(feedData))
                  }
                  "ad" -> {
                      val adsData = ADSData().apply {
                          this.feedType = feedData.feedType
                          this.mediaUrl = feedData.mediaUrl
                          this.actionId = feedData.actionId
                      }
                      tempFeedList.add(FeedOverView.Ads(adsData))
                  }
              }
          }
          if (add) {
              _feedOverviewListAdd.postValue(Event(tempFeedList))
          } else {
              _feedOverviewList.postValue(tempFeedList)
          }
      }*/


}