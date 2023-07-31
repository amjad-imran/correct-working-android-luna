package com.noisefit.ui.friends.request.received

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.Requests
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

const val FRIEND_STATUS_NONE = "0"
const val FRIEND_STATUS_ADD = "1"
const val FRIEND_STATUS_ACCEPT = "2"
const val FRIEND_STATUS_REJECTED = "3"
const val FRIEND_STATUS_REMOVE = "4"

const val FRIEND_STATUS_ADD_STRING = "sent"
const val FRIEND_STATUS_ACCEPT_STRING = "accept"
const val FRIEND_STATUS_REJECTED_STRING = "decline"
const val FRIEND_STATUS_REMOVE_STRING = "remove"


@HiltViewModel
class RequestReceivedViewModel
@Inject constructor(
    val friendsRepository: FriendsRepository,
    var sessionManager: SessionManager,
    val localDataStoredInterface: DataStoredInterface
) : BaseViewModel() {


    private val _competitionRequests = MutableLiveData<List<Requests>>()
    val competitionRequests: LiveData<List<Requests>>
        get() = _competitionRequests

    private val _allCompetitionRequests = MutableLiveData<List<Requests>>()
    val allCompetitionRequests: LiveData<List<Requests>>
        get() = _allCompetitionRequests

    private val _friendRequests = MutableLiveData<List<Requests>>()
    val friendRequests: LiveData<List<Requests>>
        get() = _friendRequests


    fun getAllCompetitionRequests() {
        viewModelScope.launch {
            friendsRepository.getAllCompetitionRequests().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _allCompetitionRequests.postValue(
                                it
                            )
                        }
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getAllCompetitionRequests()
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


    fun getReceivedRequests() {
        viewModelScope.launch {
            friendsRepository.getReceivedRequest().collect { resource ->
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
                                    getReceivedRequests()
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
        requests: Requests, status: String, updateSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                this.addProperty("friend_id", requests.user_id)
                this.addProperty("comp_detail_id", requests.comp_detail_id)
                this.addProperty("request_status", status)
            }

            friendsRepository.setCompetitionRequestStatus(requestObject).collect { resource ->
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
                                    updateCompetitionRequestStatus(requests, status, updateSuccess)
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
                            if (status == FRIEND_STATUS_ACCEPT_STRING) {
                                val count = localDataStoredInterface.getFriendCount()
                                val newCount = count + 1
                                localDataStoredInterface.setFriendCount(newCount)
                            }
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