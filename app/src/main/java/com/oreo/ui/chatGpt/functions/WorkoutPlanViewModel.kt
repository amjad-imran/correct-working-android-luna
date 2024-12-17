package com.oreo.ui.chatGpt.functions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.AiExerciseList
import com.noisefit.data.model.AiWorkoutResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.BaseViewModelCompose
import com.noisefit_commans.utils.LOGS
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WorkoutPlanViewModel @Inject constructor(
    val oreoDeviceRepository: OreoDeviceRepository
) : BaseViewModelCompose() {

    val workoutData: String? = null

    val dayTitle = MutableLiveData<String?>()
    val selectedPosition = MutableLiveData<Int>()
    val currentSelectedWeekDayPosition = MutableLiveData<Int>()
    val workoutList = MutableLiveData<List<AiExerciseList>?>()
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
                            setSelectedPosition(LocalDate.now().dayOfWeek.value)//todo based on current day
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
            it.day.equals(getDayName(position),true)
        }

        if(workout==null){
            dayTitle.postValue(null)
            workoutList.postValue(null)
        }else{
            dayTitle.postValue(null)
            workoutList.postValue(workout.exercises)
        }
    }

    private fun getDayName(position: Int): String {
        return when (position) {
            1 -> "monday"
            2 -> "tuesday"
            3 -> "wednesday"
            4 -> "thursday"
            5 -> "friday"
            6 -> "saturday"
            7 -> "sunday"
            else -> ""
        }
    }
}