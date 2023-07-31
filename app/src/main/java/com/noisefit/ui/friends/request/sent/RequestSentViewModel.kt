package com.noisefit.ui.friends.request.sent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.Requests
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestSentViewModel @Inject constructor(
    val friendsRepository: FriendsRepository
) : BaseViewModel() {

    private val _competitionRequests = MutableLiveData<List<Requests>>()
    val competitionRequests: LiveData<List<Requests>>
        get() = _competitionRequests

    private val _friendRequests = MutableLiveData<List<Requests>>()
    val friendRequests: LiveData<List<Requests>>
        get() = _friendRequests


    fun getSentRequests() {
        viewModelScope.launch {
            friendsRepository.getSentRequest().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _competitionRequests.postValue(
                                it.pendingCompetitionRequests ?: ArrayList()
                            )
                            _friendRequests.postValue(it.pendingFriendRequests ?: ArrayList())
                        }
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getSentRequests()
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
    fun updateFriendRequestStatus(requests: Requests, status: String, updateSuccess: () -> Unit) {
        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                this.addProperty("friend_id", requests.user_id)
                this.addProperty("request_status", status)
            }

            friendsRepository.setFriendRequestStatus(requestObject).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            updateSuccess()
                        }
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    updateFriendRequestStatus(requests, status, updateSuccess)
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

}