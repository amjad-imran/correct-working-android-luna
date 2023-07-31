package com.noisefit.ui.npl.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.NPLRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NplCollectSharedViewModel
@Inject
constructor(
    val nplRepository: NPLRepository,
) :
    BaseViewModel() {


    val rewardCollected = MutableLiveData<Event<Long>>()

    fun collectReward(predictionId: Long?) {
        viewModelScope.launch {
            val jsonRequest = JsonObject()
            jsonRequest.add("prediction_id_array", JsonArray().apply {
                this.add(predictionId)
            })
            nplRepository.collectNplReward(jsonRequest).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    collectReward(predictionId)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            rewardCollected.postValue(Event(predictionId))
                        }
                    }
                }
            }
        }
    }

}