package com.noisefit.ui.friends.profile.myfriend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.model.FriendsFriendListData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FeedRepository
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONException
import javax.inject.Inject

@HiltViewModel
class MyFriendViewModel @Inject constructor(
    val feedRepository: FeedRepository,
    val localStoredData: DataStoredInterface
) : BaseViewModel() {


    private val _allFriendList = MutableLiveData<List<FriendsFriendListData>>()
    val allFriendList: LiveData<List<FriendsFriendListData>>
        get() = _allFriendList

    fun getAllFriendList() {
        val jsonObject = JsonObject()
        try {
            jsonObject.addProperty("friend_id", localStoredData.getUser()?.id)
            jsonObject.addProperty("flag", "mutual")

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
}