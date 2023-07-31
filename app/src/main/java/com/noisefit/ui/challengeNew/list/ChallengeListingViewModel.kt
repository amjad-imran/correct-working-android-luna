package com.noisefit.ui.challengeNew.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.models.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeListingViewModel @Inject
constructor(
    private val userActivityRepository: UserActivityRepository,
    private val lastSyncProvider: LastSyncProvider
) : BaseViewModel() {

    var unit = Units.METRIC
    private val _newChallenges = MutableLiveData<List<com.noisefit_commans.data.response.ChallengeModel>>()
    val newChallenges: LiveData<List<com.noisefit_commans.data.response.ChallengeModel>>
        get() = _newChallenges

    private val _joinedChallenges = MutableLiveData<List<com.noisefit_commans.data.response.ChallengeModel>>()
    val joinedChallenges: LiveData<List<com.noisefit_commans.data.response.ChallengeModel>>
        get() = _joinedChallenges

    private val _completedChallenges = MutableLiveData<List<com.noisefit_commans.data.response.ChallengeModel>>()
    val completedChallenges: LiveData<List<com.noisefit_commans.data.response.ChallengeModel>>
        get() = _completedChallenges

    private val _serverReload = MutableLiveData<Boolean>(false)
    val serverReload: LiveData<Boolean>
        get() = _serverReload

    var challengeLastSyncTime: Long = 0L


    /**
     * Return count Pair(new,joined)
     */
    private val _currentCount = MutableLiveData<Pair<Int, Int>>()
    val currentCount: LiveData<Pair<Int, Int>>
        get() = _currentCount


    fun shouldReloadDetailData() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastSync = lastSyncProvider.getSyncTimeStamp(LastSyncItems.CHALLENGE_LIST_CURRENT)
            challengeLastSyncTime = lastSync
            _serverReload.postValue(lastSync.checkDayDifferenceMoreNMinutes(5))
        }
    }



    fun getCurrentChallenges(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            userActivityRepository.getCurrentChallenges(forceRefresh).collect { resource ->
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
                                    getCurrentChallenges(forceRefresh)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            it.new?.let { it1 ->
                                val list = ArrayList<com.noisefit_commans.data.response.ChallengeModel>()
                                if (it1.isNotEmpty()) {
                                    it1.forEachIndexed { i, e ->
                                        e.currentTime = it.currentTime
                                        list.add(e)
                                    }
                                }

                                _newChallenges.postValue(list)
                            }
                            it.joined?.let { it1 ->
                                val list = ArrayList<com.noisefit_commans.data.response.ChallengeModel>()
                                if (it1.isNotEmpty()) {
                                    it1.forEachIndexed { i, e ->
                                        e.currentTime = it.currentTime
                                        list.add(e)
                                    }
                                }
                                _joinedChallenges.postValue(list)
                            }

                            _currentCount.postValue(Pair(it.new?.size ?: 0, it.joined?.size ?: 0))

                        }
                        shouldReloadDetailData()
                    }
                }
            }
        }

    }

    fun getCompletedChallenges(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            userActivityRepository.getCompletedChallenges(forceRefresh).collect { resource ->
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
                                    getCurrentChallenges(forceRefresh)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _completedChallenges.postValue(it.completed ?: ArrayList())
                        }
                    }
                }
            }
        }

    }


}