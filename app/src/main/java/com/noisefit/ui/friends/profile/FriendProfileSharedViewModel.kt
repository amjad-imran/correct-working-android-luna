package com.noisefit.ui.friends.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.FriendBadge
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.ChallengeFriendListingResponse
import com.noisefit_commans.data.model.history.ActivityMessage
import com.noisefit_commans.data.model.history.StepsHistoryResponse
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendProfileSharedViewModel
@Inject
constructor(
    private val friendsRepository: FriendsRepository,
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
) : BaseViewModel() {

    var activityMessage: ActivityMessage? = null
    var userId = -1L
    var canCompete: Boolean = false
    var name:String=""

    private val _stepsHistoryResponse = MutableLiveData<StepsHistoryResponse?>()
    val stepsHistoryResponse: LiveData<StepsHistoryResponse?>
        get() = _stepsHistoryResponse

    private val _challengeListingResponse = MutableLiveData<com.noisefit_commans.data.response.ChallengeFriendListingResponse?>()
    val challengeListingResponse: LiveData<com.noisefit_commans.data.response.ChallengeFriendListingResponse?>
        get() = _challengeListingResponse

    private val _badgeListingResponse = MutableLiveData<List<FriendBadge>?>()
    val badgeListingResponse: LiveData<List<FriendBadge>?>
        get() = _badgeListingResponse


    fun setActivityData(
        data: StepsHistoryResponse?,
        activityMessage: ActivityMessage?
    ) {
        this.activityMessage = activityMessage
        _stepsHistoryResponse.postValue(data)
    }

    fun clearOldData(
        challengeFriendListingResponse: com.noisefit_commans.data.response.ChallengeFriendListingResponse?,
        friendBadgeList: List<FriendBadge>?
    ) {
        _badgeListingResponse.postValue(friendBadgeList)
        _challengeListingResponse.postValue(challengeFriendListingResponse)
    }

    fun isMyProfile(): Boolean {
        return localDataStore.getUser()?.id?.let { id ->
            return id.toLong() == userId
        } ?: false
    }


    fun getChallenges() {

        if (userId == -1L) return


        viewModelScope.launch {

            if (_challengeListingResponse.value != null) {
                _challengeListingResponse.postValue(_challengeListingResponse.value)
                return@launch
            }
            friendsRepository.getFriendChallenges(userId).collect { resource ->
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
                                    getChallenges()
                                }

                                override fun no() {

                                }
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _challengeListingResponse.postValue(it.apply {
                                this.ongoingChallenges?.forEach { challenge ->
                                    challenge.currentTime = this.currentTime
                                }
                            })
                        }
                    }
                }
            }
        }
    }


    fun getBadgesList() {

        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                if (userId != -1L) {
                    this.addProperty("friend_id", userId)
                }

            }

            if (_badgeListingResponse.value != null) {
                _badgeListingResponse.postValue(_badgeListingResponse.value)
                return@launch
            }


            friendsRepository.getFriendBadges(requestObject).collect { resource ->
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
                                    getChallenges()
                                }

                                override fun no() {

                                }
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _badgeListingResponse.postValue(it)
                        }
                    }
                }
            }
        }
    }


}