package com.noisefit.ui.friends.reactions.paginate

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit.data.base.ResourcesProvider
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FeedRepository
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionModel
import com.noisefit_commans.data.model.UserFriendData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReactionViewModel @Inject constructor(
    val feedRepository: FeedRepository, private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {

    var postId: Long? = null
    var reactionType: Int? = null
    var tabTitleList = ArrayList<String>()
    var tabIconList = ArrayList<Drawable?>()
    val emojiList = ArrayList<Emoji>()
    var reactionsData = MutableLiveData<Event<List<UserFriendData>?>>()

    private val _loadingMore = MutableLiveData<Boolean>()
    fun getLoadMoreLoading(): LiveData<Boolean> = _loadingMore

    private val _loadingInitial = MutableLiveData<Boolean>()
    fun getInitialLoading(): LiveData<Boolean> = _loadingInitial


    var has_next: Boolean? = null
    var isReactionsApiLoading = false
    var page = 0

    private val _reactionList = MutableLiveData<List<Emoji>>()
    val reactionList: LiveData<List<Emoji>>
        get() = _reactionList

    fun getInitialPostData(postId: Long) {
        viewModelScope.launch {
            feedRepository.getPostReactionsPaginate(postId, 1, 0, null).collect { resource ->
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
                                    getInitialPostData(postId)
                                }

                                override fun no() {}
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let { it ->

                            it.count?.let { it1 ->
                                has_next = it.has_next
                                reactionsData.value = Event(it.reactions)
                                generateReactionData(it1)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateReactionData(reactions: List<ReactionModel>) {

        tabTitleList.add("All")
        tabIconList.add(null)


        //add on the bases of api response
        emojiList.clear()


        val emojiHand = getReactionCount(reactions, 1)
        val emojiHeart = getReactionCount(reactions, 2)
        val emojiFire = getReactionCount(reactions, 3)
        val emoji100 = getReactionCount(reactions, 4)

        if (emojiHand > 0) {
            emojiList.add(Emoji.EmojiHand)
            tabTitleList.add("$emojiHand")
            tabIconList.add(
                resourcesProvider.getDrawable(com.noisefit_commans.R.drawable.ic_strong_emoji)
            )
        }


        if (emojiHeart > 0) {
            emojiList.add(Emoji.EmojiHeart)
            tabTitleList.add("$emojiHeart")
            tabIconList.add(
                resourcesProvider.getDrawable(com.noisefit_commans.R.drawable.ic_heart_emoji)
            )
        }


        if (emojiFire > 0) {
            emojiList.add(Emoji.EmojiFire)
            tabTitleList.add("$emojiFire")
            tabIconList.add(
                resourcesProvider.getDrawable(com.noisefit_commans.R.drawable.ic_fire_emoji)
            )
        }

        if (emoji100 > 0) {
            emojiList.add(Emoji.Emoji100)
            tabTitleList.add("$emoji100")
            tabIconList.add(
                resourcesProvider.getDrawable(com.noisefit_commans.R.drawable.ic_100_emoji)
            )
        }
        _reactionList.postValue(emojiList)

    }

    private fun getReactionCount(reactions: List<ReactionModel>, reactionType: Int): Int {
        val reaction = reactions.firstOrNull {
            it.reaction_type == reactionType
        }
        return reaction?.count ?: 0
    }


    fun getPostReactionsPaginate(postId: Long, reaction: Int?) {

        if (has_next != null && has_next == false) {
            return
        }

        isReactionsApiLoading = true


        viewModelScope.launch {
            feedRepository.getPostReactionsPaginate(postId, null, page, reaction)
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            isReactionsApiLoading = false
                            sendMessage(resource.message)
                        }
                        is Resource.Loading -> {

                            if (reactionsData.value?.peekContent().isNullOrEmpty()) {
                                _loadingInitial.postValue(resource.loading)
                            } else {
                                _loadingMore.postValue(resource.loading)
                            }
                        }
                        is Resource.NetworkError -> {
                            isReactionsApiLoading = false
                            setApiErrors(resource.response.apply {
                                this.uiComponentType as UIComponentType.RetryApiDialog
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getPostReactionsPaginate(postId, reaction)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            isReactionsApiLoading = false

                            resource.data?.data?.let { it ->
                                has_next = it.has_next
                                page += 1
                                reactionsData.postValue(Event(it.reactions))
                            }
                        }
                    }
                }
        }
    }


}