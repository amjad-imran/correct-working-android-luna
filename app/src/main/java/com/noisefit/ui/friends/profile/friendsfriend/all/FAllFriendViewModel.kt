package com.noisefit.ui.friends.profile.friendsfriend.all

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.model.FriendsFriendListData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FeedRepository
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONException
import javax.inject.Inject

@HiltViewModel
class FAllFriendViewModel @Inject constructor(
    val feedRepository: FeedRepository,
    val friendsRepository: FriendsRepository
) : BaseViewModel() {

    var friendId: String? = null

    private val _allFriendList = MutableLiveData<List<FriendsFriendListData>>()
    val allFriendList: LiveData<List<FriendsFriendListData>>
        get() = _allFriendList

    fun getAllFriendList() {
        val jsonObject = JsonObject()
        try {
            jsonObject.addProperty("friend_id", friendId)
            jsonObject.addProperty("flag", "all")

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        viewModelScope.launch {
            feedRepository.getFriendsFriendList(jsonObject).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _allFriendList.postValue(
                                it
                            )
                        }
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getAllFriendList()
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

    fun addFriendRequest(receiverId: Int, status: String, updateSuccess: () -> Unit) {

        val requestObject = JsonObject().apply {
            addProperty("request_status", status)
            addProperty("friend_id", receiverId)
        }
        viewModelScope.launch {
            friendsRepository.setFriendRequestStatus(requestObject).collect { resource ->
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
                                    addFriendRequest(
                                        receiverId, status, updateSuccess
                                    )
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            updateSuccess()
                        }
                    }
                }
            }
        }

    }

    fun removeFriendRequest(receiverId: Int, status: String, updateSuccess: () -> Unit) {
        val requestObject = JsonObject().apply {
            this.addProperty("request_status", status)
            this.addProperty("friend_id", receiverId)
        }
        viewModelScope.launch {
            friendsRepository.setFriendRequestStatus(requestObject).collect { resource ->
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
                                    removeFriendRequest(
                                        receiverId, status, updateSuccess
                                    )
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            updateSuccess()
                        }
                    }
                }
            }
        }

    }


}