package com.noisefit.ui.friends.compete

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.Competitions
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompeteListViewModel
@Inject
constructor(val friendsRepository: FriendsRepository,var sessionManager: SessionManager) : BaseViewModel() {

    var currentTime: String? = null
    private val _competeList = MutableLiveData<List<Competitions>>()
    val competeList: LiveData<List<Competitions>>
        get() = _competeList



    fun fetchCompeteList(forceRefresh: Boolean) {

        viewModelScope.launch {
            friendsRepository.getCompetitionList(forceRefresh).collect { resource ->
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
                                    fetchCompeteList(forceRefresh)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                                currentTime = it.currentTime
                                _competeList.postValue(it.competitions?:ArrayList())


                        }
                    }
                }
            }
        }

    }


}