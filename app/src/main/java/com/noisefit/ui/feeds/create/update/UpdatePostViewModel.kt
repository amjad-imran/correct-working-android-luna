package com.noisefit.ui.feeds.create.update

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.MentionUser
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FeedRepository
import com.noisefit.util.formatPostLeadingString
import com.noisefit.util.formatPostTrailingString
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.*
import javax.inject.Inject

@HiltViewModel
class UpdatePostViewModel @Inject
constructor(
    private val feedRepository: FeedRepository,
    private val localDataStore: DataStoredInterface
) : BaseViewModel() {

    var postData: TimelineData? = null
    val postSuccess = MutableLiveData<Event<Boolean>>()
    val uploadProgress = MutableLiveData<Event<Int>>()
    val mappedUser = ArrayList<MentionUser>()
    var searchText = MutableLiveData<String?>()
    var hasContent = false

    private val _friendsList = MutableLiveData<List<MentionUser>>()
    val friendsList: LiveData<List<MentionUser>>
        get() = _friendsList

    fun getUserImage(): String? {
        return localDataStore.getUser()?.imageUrl
    }

    fun updatePost(content: String) {
        viewModelScope.launch {
            uploadProgress.value = Event(0)
            feedRepository.removeOfflineFeedData()
            val leadingFormatting = content.formatPostLeadingString()
            val lengthDifference = content.length - leadingFormatting.length
            if (lengthDifference > 0) {
                mappedUser.forEach {
                    it.start_pos = (it.start_pos?.minus(lengthDifference))
                }
            }


            feedRepository.updatePost(
                leadingFormatting.formatPostTrailingString(),
                postData?.postId?.toLong() ?: -1L,
                mappedUser
            )
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }
                        is Resource.Loading -> {

                        }
                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                this.uiComponentType as UIComponentType.RetryApiDialog
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        updatePost(content)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.let {
                                uploadProgress.value = Event(100)
                                postSuccess.postValue(Event(true))
                            }
                        }
                    }
                }
        }

    }


    fun getFriendsList(spanText: String): List<MentionUser> {
        val searchText = spanText.replace("@", "")
        if (searchText.isEmpty()) return ArrayList()
        return if (_friendsList.value == null) {
            getTagFriendsList()
            ArrayList()
        } else {
            _friendsList.value?.filter {
                (it.first_name ?: "").contains(searchText, true)
            } ?: ArrayList()
        }
    }

    fun getTagFriendsList() {
        viewModelScope.launch {
            feedRepository.getTagFriendsList().collect { resource ->
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
                                    getTagFriendsList()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _friendsList.value = it
                            searchText.postValue(searchText.value)
                        }
                    }
                }
            }
        }

    }

    fun addTagMapping(user: MentionUser, start: Int) {
        mappedUser.add(
            MentionUser(
                id = user.id,
                first_name = user.first_name,
                start_pos = start
            )
        )
    }
    fun getFeedPosCount(): Int {
        return localDataStore.getFeedPostCreateCount()
    }

    fun updateFeedPostCount(count:Int){
        localDataStore.setFeedPostCreateCount(count)
    }

}