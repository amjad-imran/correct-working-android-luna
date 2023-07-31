package com.noisefit.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.data.handleReactionData
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.FriendProgress
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.models.DurationRange
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.launch
import org.json.JSONException
import javax.inject.Inject

@HiltViewModel
class FriendListViewModel
@Inject
constructor(
    val friendsRepository: FriendsRepository,
    var sessionManager: SessionManager,
    val localDataStore: DataStoredInterface
) : BaseViewModel() {

    var duration = MutableLiveData(DurationRange.TODAY.name)
    var startDate = MutableLiveData<String>()
    var endDate = MutableLiveData<String>()
    var insightValue = MutableLiveData<String>()

    var requestCount = MutableLiveData<Int>()

    private val _friendList = MutableLiveData<List<FriendProgress>?>(null)
    val friendList: LiveData<List<FriendProgress>?>
        get() = _friendList

    private val _userFriendReactions = MutableLiveData<Event<ArrayList<ReactionsWrapper>>>()
    val userFriendReactions: LiveData<Event<ArrayList<ReactionsWrapper>>>
        get() = _userFriendReactions


    fun getFriendListData(forceRefresh: Boolean) {
        viewModelScope.launch {
            friendsRepository.getFriendList(
                forceRefresh,
                startDate.value.toString(),
                endDate.value.toString(),
                duration.value.toString()
            )
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
                                        getFriendListData(forceRefresh)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data?.let { it ->
                                val progressData = ArrayList<FriendProgress>()
                                if (!it.progress.isNullOrEmpty()) {
                                    for (i in it.progress!!.indices) {
                                        progressData.add(it.progress!![i])
                                    }
                                }
                                insightValue.value = it.insight
                                _friendList.value = (progressData)
                                requestCount.postValue(it.requests_count ?: 0)
                            }
                        }
                    }
                }
        }
    }

    private fun postEmojiToUser(userName: String, emojiType: String?, id: Int) {
        val jsonObject = JsonObject()
        try {
            emojiType?.let {
                jsonObject.addProperty("emojis_type", emojiType)
            }
            jsonObject.addProperty("friend_id", id)

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
                                        postEmojiToUser(userName, emojiType, id)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data?.let { it ->
                                //you have appreciated
//                                emojiType?.let {
//                                    val message = "You have appreciated $userName"
//                                    updateAppreciateMessage(message)
//                                }
                            }
                        }
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

    fun updateFriendList(userName: String, position: Int, emoji: Emoji?) {
        _friendList.value?.get(position)?.let { friendProgress ->
            if (emoji == null) {
                friendProgress.userEmoji = null
                postEmojiToUser(userName, null, friendProgress.user_id)
            } else {
                postEmojiToUser(userName, emoji.emoji, friendProgress.user_id)
                friendProgress.userEmoji = emoji.emoji
            }
        }
    }


    fun getDurationValue(): String {
        val tempDuration: String =
            if (duration.value?.lowercase() == DurationRange.PREVIOUS_WEEK.type.lowercase())
                "Previous Week"
            else if (duration.value?.lowercase() == DurationRange.MONTHLY.type.lowercase())
                "Monthly"
            else if (duration.value?.lowercase() == DurationRange.TODAY.type.lowercase())
                "Today"
            else if (duration.value?.lowercase() == DurationRange.YESTERDAY.type.lowercase())
                "Yesterday"
            else
                "This week"
        return tempDuration.replaceFirstChar { if (it.isLowerCase()) it.titlecase(DateFormats.defaultLocale) else it.toString() }
    }

    fun setDuration(durations: String?) {
        val temp: String = when (durations?.lowercase()) {
            "previous week" -> DurationRange.PREVIOUS_WEEK.type
            "monthly" -> DurationRange.MONTHLY.type
            "today" -> DurationRange.TODAY.type
            "yesterday" -> DurationRange.YESTERDAY.type
            else -> DurationRange.THIS_WEEK.type
        }
        duration.value = temp
    }


}