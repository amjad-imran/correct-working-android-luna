package com.oreo.ui.chatGpt.functions

import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.AiWorkout
import com.noisefit.data.model.AiWorkoutResponse
import com.noisefit.data.model.AiWorkouts
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WorkoutPlanViewModel @Inject constructor(
    val oreoDeviceRepository: OreoDeviceRepository,
    val localDataStore: DataStoredInterface,
) : BaseViewModel() {

    val workoutData: String? = null

    val dayTitle = MutableLiveData<String?>()
    val selectedPosition = MutableLiveData<Int>()
    val currentSelectedWeekDayPosition = MutableLiveData<Int>()
    val workoutList = MutableLiveData<Pair<List<AiWorkout>?, DietState>>()
    private val workoutResponse = ArrayList<AiWorkoutResponse>()
    private var relaxedWorkoutResponse = AiWorkoutResponse()

    val workoutState = MutableLiveData<DietState>()

    init {
        currentSelectedWeekDayPosition.postValue(LocalDate.now().dayOfWeek.value)
    }

    var isComfortEnabled = false

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

                            val curDay = LocalDate.now().dayOfWeek.value
                            val isCurrentDayRest = isCurrentDayRest(curDay)

                            if(!isCurrentDayRest && isComfortEnabled){
                                getRelaxedWorkoutPlans()
                            }
                            else {
                                if (
                                    !localDataStore.getLdwReadinessData() &&
                                    !localDataStore.getLdwCycleTrackerData()
                                ) {
                                    workoutState.value = DietState.NORMAL
                                    setSelectedPosition(curDay)
                                } else {
                                    if (isCurrentDayRest) {
                                        workoutState.value = DietState.NORMAL
                                        setSelectedPosition(curDay)
                                    } else {
                                        val isLowWorkoutPlanSetUp =
                                            localDataStore.isLowWorkoutPlanSetUp()?.isSetup ?: false
                                        if (isLowWorkoutPlanSetUp) {
                                            getRelaxedWorkoutPlans()
                                        } else {
                                            workoutState.value = DietState.REGULAR
                                            setSelectedPosition(curDay)
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private fun isCurrentDayRest(position: Int): Boolean{
        val workout = workoutResponse.find {
            it.day_name.equals(getDayName(position), true)
        }

        val mergedWorkouts = getAllWorkouts(workout?.workouts)

        return mergedWorkouts.isEmpty() || isRestDay(mergedWorkouts)
    }

    fun getRelaxedWorkoutPlans() {
        viewModelScope.launch {
            oreoDeviceRepository.getAiRelaxedWorkoutPlans().collect { resource ->
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
                            workoutState.value = DietState.COMFORT
                            relaxedWorkoutResponse = it
                            setSelectedPosition(LocalDate.now().dayOfWeek.value)
                            localDataStore.setIsWorkoutPlanSetUp(true)
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

        val mergedWorkouts = getAllWorkouts(workout?.workouts)


        if (mergedWorkouts.isEmpty()/*workout?.workouts?.firstOrNull()?.workout.isNullOrEmpty()*/) {
            dayTitle.postValue(null)
            workoutList.postValue(Pair(null, DietState.NORMAL))
        } else {
            //val workouts = workout?.workouts?.firstOrNull()!!.workout

            if (isRestDay(mergedWorkouts)) {
                dayTitle.postValue(null)
                workoutList.postValue(Pair(ArrayList(), DietState.NORMAL))
            } else {
                if(position == LocalDate.now().dayOfWeek.value) {
                    if (workoutState.value==DietState.COMFORT) {
                        val relaxedWorkout = relaxedWorkoutResponse.workouts
                        val sessionName = relaxedWorkout?.firstOrNull()?.session
                        dayTitle.postValue(sessionName)
                        workoutList.postValue(Pair(getAllWorkouts(relaxedWorkout), DietState.COMFORT))
                    } else {
                        val sessionName = workout?.workouts?.firstOrNull()?.session
                        dayTitle.postValue(sessionName)
                        workoutList.postValue(Pair(mergedWorkouts, workoutState.value?:DietState.NORMAL))
                    }
                }else{
                    val sessionName = workout?.workouts?.firstOrNull()?.session
                    dayTitle.postValue(sessionName)
                    workoutList.postValue(Pair(mergedWorkouts, DietState.NORMAL))
                }
            }
        }
    }

    private fun getAllWorkouts(workouts: List<AiWorkouts>?): List<AiWorkout> {
        if (workouts.isNullOrEmpty()) return ArrayList()

        val merged = ArrayList<AiWorkout>()
        workouts.forEach {
            if (it.workout.isNullOrEmpty().not()) {
                merged.addAll(it.workout!!)
            }
        }

        return merged
    }

    private fun isRestDay(workouts: List<AiWorkout>?): Boolean {
        return workouts?.firstOrNull()?.reps.isNullOrEmpty()
    }

    private fun getDayName(position: Int): String {
        return "day_$position"
    }

    fun getTextShaderForGradient(
        width: Float,
        startGradColor: Int,
        endGradColor: Int
    ): LinearGradient {
        return LinearGradient(
            0f, 0f, width, 0f,  // Left to right gradient
            intArrayOf(
                startGradColor,  // start color
                endGradColor   // end color
            ),
            null,
            Shader.TileMode.CLAMP
        )
    }
}