package com.noisefit.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.BuddiesUserNew
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommonFriendsViewModel @Inject constructor(
    private val friendsRepository: FriendsRepository,
) : BaseViewModel() {


    var friendId: Int? = null

    private val _friendsList = MutableLiveData<List<BuddiesUserNew>>()
    val friendsList: LiveData<List<BuddiesUserNew>>
        get() = _friendsList


    fun getCommonFriends() {
        viewModelScope.launch {
            val requestObjet = JsonObject().apply {
                addProperty("friendId", friendId)
            }
            friendsRepository.getCommonFriends(requestObjet).collect { resource ->
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
                                    getCommonFriends()
                                }

                                override fun no() {

                                }
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _friendsList.postValue(it)
                        }
                    }
                }
            }
        }
    }


}