package com.oreo.ui.workout.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OWorkoutDetailsViewModel @Inject constructor(
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

    var position: Int = -1
    private val _workoutDeletedResponse = MutableLiveData<Event<Boolean>>()
    val workoutDeletedResponse: LiveData<Event<Boolean>> = _workoutDeletedResponse
    private val _workoutDetailsResponse = MutableLiveData<OWorkoutDetailsResponseModel>()
    val workoutDetailsResponse: LiveData<OWorkoutDetailsResponseModel> = _workoutDetailsResponse
    fun getWorkoutDetails(workoutId: String) {
        viewModelScope.launch {
            userActivityRepository.getWorkoutDetails(
                workoutId
            ).collect { resource ->
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
                                        getWorkoutDetails(workoutId)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _workoutDetailsResponse.postValue(it)
                        }
                    }
                }
            }
        }

    }

    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch {
            userActivityRepository.deleteWorkoutFromServer(
                workoutId
            ).collect { resource ->
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
                                        deleteWorkout(workoutId)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            sendMessage("Workout Deleted")
                            _workoutDeletedResponse.postValue(Event(true))
                        }
                    }
                }
            }
        }

    }
}