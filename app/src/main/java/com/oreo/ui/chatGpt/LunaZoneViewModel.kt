package com.oreo.ui.chatGpt

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.SuggestedAiQuestions
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LunaZoneViewModel @Inject constructor(
    val sessionManager: SessionManager,
    private val deviceRepository: OreoDeviceRepository,
    private val ringDataStore: RingDataStore
) : BaseViewModel() {

    val suggestedQuestions = MutableLiveData<List<SuggestedAiQuestions>>()
    val CARD_RATIO = 0.55

    /**
     * Pair(workout,meal) state
     */
    val planState = MutableLiveData<Pair<Boolean, Boolean>>()
    val summaryStates = MutableLiveData<SummaryStates>()

    fun getLunaZoneData() {
        viewModelScope.launch {
            deviceRepository.getLunaZoneData().collect { resource ->
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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getLunaZoneData()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            planState.postValue(
                                Pair(
                                    it.workoutPlan ?: false,
                                    it.nutritionalPlan ?: false
                                )
                            )

                            summaryStates.postValue(getSummaryStates(it.summaryAvailable))
                            //summaryStates.postValue(SummaryStates.NO_DEVICE)

                            suggestedQuestions.postValue(it.suggestedQues ?: ArrayList())

                        }
                    }
                }
            }
        }
    }

    private fun getSummaryStates(summaryAvailable: Boolean?): SummaryStates {
        if (summaryAvailable == true) {
            return SummaryStates.DATA_AVAILABLE
        }
        val isDeviceConnected = ringDataStore.getRingDevice() != null
        if (isDeviceConnected.not()) {
            return SummaryStates.NO_DEVICE
        } else {
            return SummaryStates.NO_DATA
        }
    }

}

enum class SummaryStates {
    NO_DEVICE, NO_DATA, GENERATING, DATA_AVAILABLE, NONE
}