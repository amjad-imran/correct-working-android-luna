package com.noisefit.ui.friends.compete.withFriends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.CompeteFriend
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompeteFriendListViewModel
@Inject
constructor(
    val friendsRepository: FriendsRepository,
    val localDataStore: DataStoredInterface,
    var sessionManager: SessionManager
) : BaseViewModel() {


    var rule: String? = null
    private val _competeFriendList = MutableLiveData<List<CompeteFriend>>()
    val competeFriendList: LiveData<List<CompeteFriend>>
        get() = _competeFriendList


    init {

        fetchCompeteFriendList()
    }

    fun updateCompetitionRequestStatus(
        receiverId: Int,
        status: String,
        updateSuccess: () -> Unit
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
                                    updateCompetitionRequestStatus(receiverId, status, updateSuccess)
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

    fun fetchCompeteFriendList() {

        if (_competeFriendList.value != null) {
            _competeFriendList.value = _competeFriendList.value
            return
        }

        viewModelScope.launch {
            friendsRepository.getCompeteFriendsList().collect { resource ->
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
                                    fetchCompeteFriendList()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            rule = it.rule
                            _competeFriendList.postValue(it.competeFriend ?: ArrayList())

                        }
                    }
                }
            }
        }

    }


}