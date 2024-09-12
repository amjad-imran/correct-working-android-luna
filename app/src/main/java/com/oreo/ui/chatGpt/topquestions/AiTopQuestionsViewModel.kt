package com.oreo.ui.chatGpt.topquestions

import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiTopQuestionsViewModel @Inject constructor(
    val oreoDeviceRepository: OreoDeviceRepository
) : BaseViewModel() {


    fun getWorkoutList(postValue: Boolean) {
        viewModelScope.launch {
            oreoDeviceRepository.getAiTopQuestions().collect { resource ->
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
                                        getWorkoutList(postValue)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (postValue) {
                                _oWorkoutListModalResponse.postValue(it)
                            } else {
                                val walkingWorkout =
                                    it.find { it.activityType.equals("walking", true) }
                                if (autoSport.value == null) {

                                    walkingWorkout?.let { walk ->
                                        updateDefaultWorkout.postValue(Event(walk))
                                    }
                                } else {
                                    workoutListModal = walkingWorkout
                                }

                            }
                        }
                    }
                }
            }
        }


    }

}