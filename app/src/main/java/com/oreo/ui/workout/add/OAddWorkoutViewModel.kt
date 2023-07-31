package com.oreo.ui.workout.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.OAddWorkout
import com.oreo.data.model.OWorkoutListModal
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OAddWorkoutViewModel
@Inject
constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    private val localDatSource: DataStoredInterface,
) : BaseViewModel() {

    val minimumWorkoutTime = 10
    val maxWorkoutTime = 180
    private val _addWorkoutResponse = MutableLiveData<Boolean>()
    val addWorkoutResponse = _addWorkoutResponse
    private val _oWorkoutListModalResponse = MutableLiveData<List<OWorkoutListModal>>()
    val oWorkoutListModalResponse: LiveData<List<OWorkoutListModal>> = _oWorkoutListModalResponse
    var addWorkout = OAddWorkout()
    var workoutListModal: OWorkoutListModal? = null


    fun addWorkout() {

        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                this.addProperty("duration", addWorkout.duration)
                this.addProperty("calories", addWorkout.calories)
                this.addProperty("activity_type", workoutListModal?.activityType)
                this.addProperty("start_time", addWorkout.startTimeIn24H)
                this.addProperty("end_time", addWorkout.endTimeIn24H)
                this.addProperty("intensity", addWorkout.intensity)
            }
            userActivityRepository.addWorkout(
                requestObject
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
                                        addWorkout()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _addWorkoutResponse.postValue(true)

                        }
                    }
                }
            }
        }
    }
    fun getWorkoutDuration(): Int {
        val diffInHours = addWorkout.endHour - addWorkout.startHour

        return (diffInHours * 60) + (addWorkout.endMinute - addWorkout.startMinute)
    }

    fun getCaloriesBurnt(): Float {

        LOGS.d("getCaloriesBurnt ${addWorkout.duration} ${addWorkout.intensity} ${workoutListModal}")
        if (addWorkout.duration == 0) {
            return 0f
        }

        if (addWorkout.intensity.isEmpty()) {
            return 0f
        }

        if (workoutListModal == null) {
            return 0f
        }

        val weight = localDatSource.getUser()?.userInfo?.weight ?: 1

        when (addWorkout.intensity.lowercase()) {
            "easy" -> {
                return addWorkout.duration * (workoutListModal?.lowIntensity ?: 0f) * weight
            }

            "moderate" -> {
                return addWorkout.duration * (workoutListModal?.mediumIntensity ?: 0f) * weight
            }

            "hard" -> {
                return addWorkout.duration * (workoutListModal?.highIntensity ?: 0f) * weight
            }
        }
        return 0f
    }

    fun getWorkoutList() {
        viewModelScope.launch {

            userActivityRepository.getWorkoutList().collect { resource ->
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
                                        getWorkoutList()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _oWorkoutListModalResponse.postValue(it)
                        }
                    }
                }
            }
        }


    }
}