package com.noisefit.ui.friends.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.handleReactionData
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.FriendProfile
import com.noisefit_commans.data.model.MyEmoji
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_ACCEPT_STRING
import com.noisefit.ui.friends.request.received.*
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import javax.inject.Inject

@HiltViewModel
class FriendProfileViewModel @Inject constructor(
    private val friendsRepository: FriendsRepository,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {

    val navigateUp = MutableLiveData<Event<Boolean>>()

    val showEditProfile = MutableLiveData<Event<Boolean>>()
    val showGuidelines = MutableLiveData<Event<Boolean>>()
    val callComputeNow = MutableLiveData<Event<Boolean>>()
    val callRemoveFriend = MutableLiveData<Event<Boolean>>()

    private val _userFriendReactions = MutableLiveData<Event<ArrayList<ReactionsWrapper>>>()
    val userFriendReactions: LiveData<Event<ArrayList<ReactionsWrapper>>>
        get() = _userFriendReactions

    private val _friendProfile = MutableLiveData<FriendProfile>()
    val friendProfile: LiveData<FriendProfile>
        get() = _friendProfile

    private val _userEmoji = MutableLiveData<String?>()
    val userEmoji: LiveData<String?>
        get() = _userEmoji


    private val _myEmojis = MutableLiveData<MyEmoji?>()
    val myEmojis: LiveData<MyEmoji?>
        get() = _myEmojis

    var isAccepted = false

    var isMyProfile = false
    var profileId = -1L

    var localUserId = -1

    init {
        localUserId = localDataStore.getUser()?.id ?: -1
    }


    fun getFriendProfile(forceRefresh: Boolean) {
        viewModelScope.launch {
            val requestObjet = JsonObject().apply {
                if (profileId != -1L && !isMyProfile) {
                    addProperty("userId", profileId)
                }
            }
            friendsRepository.getFriendProfile(forceRefresh, requestObjet, profileId)
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
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getFriendProfile(forceRefresh)
                                    }

                                    override fun no() {

                                    }
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data?.let {
                                if (it.user_id == 0L && profileId != -1L) {
                                    it.user_id = profileId
                                }
                                _friendProfile.postValue(it)

                                if (shouldShowUserEmoji(it.request_status ?: 0)) {
                                    _userEmoji.postValue(it.user_emoji)
                                }
                                _myEmojis.postValue(it.myEmoji)
                            }
                        }
                    }
                }
        }
    }

    private fun shouldShowUserEmoji(requestStatus: Int): Boolean {
        if (isMyProfile) {
            return false
        } else {
            if (requestStatus == FRIEND_STATUS_ACCEPT.toInt()) {
                return true
            }
        }
        return false
    }

    fun updateFriendRequestStatus(userId: Int, status: String) {
        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                this.addProperty("friend_id", userId)
                this.addProperty("request_status", status)
            }

            friendsRepository.setFriendRequestStatus(requestObject).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            isAccepted = status == FRIEND_STATUS_ACCEPT_STRING
                            if (status == FRIEND_STATUS_REMOVE_STRING) {
                                navigateUp.postValue(Event(true))
                            } else {
                                getFriendProfile(true)
                            }
                        }
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    updateFriendRequestStatus(userId, status)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                }

            }
        }
    }


    fun updateCompetitionRequestStatus(
        receiverId: Int, status: String
    ) {
        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                this.addProperty("friend_id", receiverId)
                this.addProperty("request_status", status)
            }

            friendsRepository.setCompetitionRequestStatus(requestObject).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            friendProfile.value?.canCompete = false
                            _friendProfile.postValue(friendProfile.value)
                        }
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    updateCompetitionRequestStatus(receiverId, status)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                }

            }
        }
    }

    fun updateEmojiLocally(userName: String?, emoji: Emoji?) {
        _friendProfile.value?.let {
            if (emoji == null) {
                it.user_emoji = null
                postEmojiToUser(userName, null)
                _userEmoji.postValue(null)
                viewModelScope.launch(Dispatchers.IO) {
                    friendsRepository.updateEmojiLocally(null, it.user_id)
                }

            } else {
                postEmojiToUser(userName, emoji.emoji)
                it.user_emoji = emoji.emoji
                _userEmoji.postValue(emoji.emoji)
                viewModelScope.launch(Dispatchers.IO) {
                    friendsRepository.updateEmojiLocally(emoji.emoji, it.user_id)
                }
            }
        }
    }

    fun getUserFriendEmojiData() {
        viewModelScope.launch {
            friendsRepository.getUserFriendEmoji()
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
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getUserFriendEmojiData()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data?.let { it ->
                                _userFriendReactions.postValue(Event(it.handleReactionData()))
                            }
                        }
                    }
                }
        }
    }

    private fun postEmojiToUser(userName: String?, emojiType: String?) {
        val jsonObject = JsonObject()
        try {
            emojiType?.let {
                jsonObject.addProperty("emojis_type", emojiType)
            }
            jsonObject.addProperty("friend_id", profileId)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        viewModelScope.launch {
            friendsRepository.postEmojiToUser(jsonObject, true)
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }
                        is Resource.Loading -> {
//                            setLoading(resource.loading)
                        }
                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        postEmojiToUser(userName, emojiType)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data?.let { it ->

                            }
                        }
                    }
                }
        }
    }


}

enum class UerType {
    USER, INFLUENCER, ADMIN
}