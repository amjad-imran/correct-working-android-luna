package com.noisefit.ui.feeds.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.model.MentionUser
import com.noisefit.data.model.timeline.CommentData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FeedRepository
import com.noisefit.session.SessionManager
import com.noisefit.util.formatPostLeadingString
import com.noisefit.util.formatPostTrailingString
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.User
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostCommentViewModel
@Inject
constructor(
    private val feedRepository: FeedRepository,
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK,
) : BaseViewModel() {
    private var _userCommentList = MutableLiveData<ArrayList<CommentData>>()
    var userCommentList: LiveData<ArrayList<CommentData>> = _userCommentList

    var showDeleteBottomSheet = MutableLiveData<Event<Boolean>>()
    private var _commentPosted = MutableLiveData<Event<Boolean>>()
    var commentPosted: LiveData<Event<Boolean>> = _commentPosted

    var postId: Long? = null
    var taggedUser = ArrayList<MentionUser>()
    var commentId: Long? = null
    var user: User? = null
    var caption: String = ""

    init {
        user = localDataStore.getUser()
    }

    fun updateDeleteBottomSheet(status: Boolean) {
        showDeleteBottomSheet.postValue(Event(status))
    }

    fun getComment() {

        viewModelScope.launch {
            feedRepository.getComment(postId!!).collect { resource ->
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
                                    getComment()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _userCommentList.postValue(it)

                            feedRepository.updateOfflineCommentsData(postId!!, it,0)

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
            feedRepository.addComment(postId!!, jsonObject).collect { resource ->
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
                                _userCommentList.value?.add(it[0])
                                _commentPosted.postValue(Event(true))
                                _userCommentList.postValue(_userCommentList.value)

                                feedRepository.updateOfflineCommentsData(
                                    postId,
                                    _userCommentList.value,
                                0)
                            }


                        }
                    }
                }
            }
        }
    }

    fun removeComment(commentId: Long?) {
        if (commentId == null) return

        val lastValue = _userCommentList.value
        val index = lastValue?.indexOfFirst {
            it.commentId == commentId
        }

        tryCatch {
            if (index != null && index != -1) {
                lastValue?.removeAt(index)
                lastValue?.let {
                    _userCommentList.postValue(it)

                    viewModelScope.launch {
                        feedRepository.updateOfflineCommentsData(
                            postId,
                            _userCommentList.value,
                            0
                        )
                    }
                }
            }
        }
    }


}