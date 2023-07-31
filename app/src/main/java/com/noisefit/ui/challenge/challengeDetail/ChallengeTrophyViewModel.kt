package com.noisefit.ui.challenge.challengeDetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeTrophyViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    var trophyTitle: String? = null
    var trophyUrl: String? = null
    var activityId: Int? = null
    var isTrophyCollected: Boolean? = false
    val badgeCollected: MutableLiveData<Event<Boolean>> = MutableLiveData()


    fun collectChallengeTrophy(activityId: Int) {
        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                this.addProperty("activityId", activityId)
            }

            userRepository.collectChallengeTrophy(requestObject).collect { resource ->
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
                                    collectChallengeTrophy(activityId)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.let { response ->
                            sendMessage(response.message)
                            badgeCollected.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }

}