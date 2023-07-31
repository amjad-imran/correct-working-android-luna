package com.noisefit.ui.friends.compete

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendSharedViewModel
@Inject constructor(
    var sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    private val friendsRepository: FriendsRepository
) : BaseViewModel() {


    var showReactionSheet = false
    var friendsListSize: Int = -1

    var requestCount = MutableLiveData<Int>()


    fun getPendingRequestCount() {
        viewModelScope.launch {

            friendsRepository.getPendingRequestCount()
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data?.data?.let {
                                requestCount.postValue(it.requestsCount ?: 0)
                            }
                        }
                        else -> {}
                    }
                }
        }
    }


}