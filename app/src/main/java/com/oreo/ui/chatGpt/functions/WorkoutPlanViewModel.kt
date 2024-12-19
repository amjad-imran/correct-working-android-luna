package com.oreo.ui.chatGpt.functions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.AiWorkout
import com.noisefit.data.model.AiWorkoutResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WorkoutPlanViewModel @Inject constructor(
    val oreoDeviceRepository: OreoDeviceRepository
) : BaseViewModel() {

    val workoutData: String? = null

    val dayTitle = MutableLiveData<String?>()
    val selectedPosition = MutableLiveData<Int>()
    val currentSelectedWeekDayPosition = MutableLiveData<Int>()
    val workoutList = MutableLiveData<List<AiWorkout>?>()
    private val workoutResponse = ArrayList<AiWorkoutResponse>()

    init {
        currentSelectedWeekDayPosition.postValue(LocalDate.now().dayOfWeek.value)
    }

    fun getWorkoutPlans() {
        viewModelScope.launch {
            oreoDeviceRepository.getAiWorkoutPlans().collect { resource ->
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
                                        getWorkoutPlans()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            workoutResponse.clear()
                            workoutResponse.addAll(it)
                            setSelectedPosition(LocalDate.now().dayOfWeek.value)
                        }
                    }
                }
            }
        }
    }

    /**
     *@param position-> 1..7 (Mon - Sun)
     */
    fun setSelectedPosition(position: Int) {
        selectedPosition.postValue(position)

        val workout = workoutResponse.find {
            it.day_name.equals(getDayName(position), true)
        }

        if (workout?.workouts?.firstOrNull()?.workout.isNullOrEmpty()) {
            dayTitle.postValue(null)
            workoutList.postValue(null)
        } else {
            val workouts = workout?.workouts?.firstOrNull()!!.workout

            if (isRestDay(workouts)) {
                dayTitle.postValue(null)
                workoutList.postValue(ArrayList())
            } else {
                dayTitle.postValue(workout.workouts.firstOrNull()?.session)
                workoutList.postValue(workouts)
            }
        }
    }

    private fun isRestDay(workouts: List<AiWorkout>?): Boolean {
        return workouts?.firstOrNull()?.reps.isNullOrEmpty()
    }

    private fun getDayName(position: Int): String {
        return "day_$position"
    }
}