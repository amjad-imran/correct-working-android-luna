package com.noisefit.ui.challenge.challengeLeaderboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.Leadership
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit_commans.data.model.challenge.ChallengeIds
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeLeaderboardViewModel
@Inject
constructor(
    private val userActivityRepository: UserActivityRepository,
    private val friendsRepository: FriendsRepository,
    var sessionManager: SessionManager
) : BaseViewModel() {

    var ids: ChallengeIds? = null
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _allLeaders = MutableLiveData<ArrayList<Leadership>>()
    val allLeaders: LiveData<ArrayList<Leadership>>
        get() = _allLeaders

    private val _buddyLeaders = MutableLiveData<ArrayList<Leadership>>()
    val buddyLeaders: LiveData<ArrayList<Leadership>>
        get() = _buddyLeaders

    var mLastClickTime: Long? = null


    fun getLeaderboard(forceRefresh: Boolean, ids: ChallengeIds) {
        this.ids = ids
        viewModelScope.launch {
            userActivityRepository.getChallengeLeaderboard(forceRefresh, ids.id.toString())
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {

                        }
                        is Resource.Loading -> {
                            _isLoading.postValue(resource.loading)
                        }
                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getLeaderboard(forceRefresh, ids)
                                    }

                                    override fun no() {}
                                }
                            })

                        }
                        is Resource.Success -> {
                            resource.data?.data?.let {
                                if (it != null && it.isNotEmpty()) {
                                    _allLeaders.postValue(it)
                                }
                            }
                        }
                    }
                }
        }
    }

    fun getBuddiesLeaderboard(forceRefresh: Boolean, ids: ChallengeIds) {
        this.ids = ids
        viewModelScope.launch {
            userActivityRepository.getChallengeBuddyLeaderboard(forceRefresh, ids.id.toString())
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {

                        }
                        is Resource.Loading -> {
                            _isLoading.postValue(resource.loading)
                        }
                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getBuddiesLeaderboard(forceRefresh, ids)
                                    }

                                    override fun no() {}
                                }
                            })

                        }
                        is Resource.Success -> {
                            resource.data?.data?.let {
                                _buddyLeaders.postValue(it)
                            }
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



}