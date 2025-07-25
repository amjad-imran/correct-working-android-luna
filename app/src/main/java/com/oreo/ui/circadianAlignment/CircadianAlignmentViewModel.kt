package com.oreo.ui.circadianAlignment

import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CircadianMidPointModel
import com.oreo.data.model.CircadianMidPointState
import com.oreo.data.model.CircadianMidPointStatus
import com.oreo.data.model.CorrectiveActivitiesModel
import com.oreo.data.model.OnlyImgWithText
import com.oreo.data.model.ProgressBarLytData
import com.oreo.data.model.circadian.Activity
import com.oreo.data.model.circadian.CircadianResponseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class CircadianAlignmentViewModel @Inject constructor(
    private val resourceProvider: ResourcesProvider,
    private val userRepository: UserRepository
): BaseViewModel() {

    companion object{
        const val light_exposure_key = "light_exposure"
        const val daily_steps_key = "daily_steps"
        const val meal_window_key = "meal_window"
        const val caffeine_window_key = "caffeine_window"
        const val workout_key = "workout"
        val actMonStatusList = listOf("partial", "done")
    }

    val lightExposureData = MutableLiveData<CorrectiveActivitiesModel>()
    val dailyStepsData = MutableLiveData<CorrectiveActivitiesModel>()
    val mealWindowData = MutableLiveData<CorrectiveActivitiesModel>()
    val workoutData = MutableLiveData<CorrectiveActivitiesModel>()
    val caffeineWindowData = MutableLiveData<CorrectiveActivitiesModel>()

    val circadianResponseData = MutableLiveData<CircadianResponseModel>()

    val correctiveActivitiesListData = MutableLiveData<ArrayList<CorrectiveActivitiesModel>>()

    init {
        initData()
    }

    fun initData(){
        viewModelScope.launch {
            userRepository.getCircadianData().collect{ resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {

                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            circadianResponseData.postValue(it)
                            prepareCorrectiveActivitiesData(it.activities)
                            LOGS.d("abcjacjcab Posting data: $it")
                        }
                    }
                }
            }
        }
    }

    private fun prepareCorrectiveActivitiesData(activitiesData: List<Activity>){
        activitiesData.forEach {

            val isLogged = it.status?.let { ss -> ss=="Logged" }
            val timeLeft =
                if (it.time == null || it.time == 0) {
                    null
                }else{
                    val calcTime = formatSecondsToHHMM(it.time)
                    resourceProvider.getString(R.string.text_timeval_left, calcTime)
                }

            when(it.type){
                // Light Exposure Data
                light_exposure_key -> {
                    lightExposureData.value = CorrectiveActivitiesModel(
                        key = light_exposure_key,
                        bgMainImg = R.drawable.bg_light_exposure_corrective_activities,
                        title = resourceProvider.getString(R.string.text_light_exposure),
                        desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
                        onlyImgWithText = OnlyImgWithText(
                            img = R.drawable.ic_sun_activity_monitor,
                            txt = "45\nmins"
                        ),
                        progressBarLytData = null,
                        logStatus = isLogged,
                        time = timeLeft
                    )
                }

                daily_steps_key -> {
                    // Daily Steps Data
                    dailyStepsData.value = CorrectiveActivitiesModel(
                        key = daily_steps_key,
                        bgMainImg = R.drawable.bg_daily_steps_corrective_activities,
                        title = resourceProvider.getString(R.string.text_daily_steps),
                        desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
                        onlyImgWithText = null,
                        progressBarLytData = ProgressBarLytData(
                            totalProgress = 100,
                            currentProgress = 60,
                            img = R.drawable.ic_shoe_corrective_activities,
                            txt = "2334"
                        ),
                        logStatus = isLogged,
                        time = timeLeft
                    )
                }

                meal_window_key -> {
                    // Meal Window Data
                    mealWindowData.value = CorrectiveActivitiesModel(
                        key = meal_window_key,
                        bgMainImg = R.drawable.bg_meal_window_corrective_activities,
                        title = resourceProvider.getString(R.string.text_meal_window),
                        desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
                        onlyImgWithText = OnlyImgWithText(
                            img = R.drawable.ic_meal_corrective_activities,
                            txt = null
                        ),
                        progressBarLytData = null,
                        logStatus = isLogged,
                        time = timeLeft
                    )
                }

                workout_key -> {
                    // Workout Data
                    workoutData.value = CorrectiveActivitiesModel(
                        key = workout_key,
                        bgMainImg = R.drawable.bg_workout_corrective_activities,
                        title = resourceProvider.getString(R.string.text_workout),
                        desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
                        onlyImgWithText = null,
                        progressBarLytData = ProgressBarLytData(
                            totalProgress = 100,
                            currentProgress = 60,
                            img = R.drawable.ic_workout_corrective_activities,
                            txt = "23 mins"
                        ),
                        logStatus = isLogged,
                        time = timeLeft
                    )
                }

                caffeine_window_key -> {
                    // Caffeine Window Data
                    caffeineWindowData.value = CorrectiveActivitiesModel(
                        key = caffeine_window_key,
                        bgMainImg = R.drawable.bg_caffeine_window_corrective_activities,
                        title = resourceProvider.getString(R.string.text_caffeine_window2),
                        desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
                        onlyImgWithText = OnlyImgWithText(
                            img = R.drawable.ic_caffeine_corrective_activities,
                            txt = null
                        ),
                        progressBarLytData = null,
                        logStatus = isLogged,
                        time = timeLeft
                    )
                }
            }
        }

        val correctiveActivitiesList:
                ArrayList<CorrectiveActivitiesModel> = ArrayList()

        lightExposureData.value?.let { correctiveActivitiesList.add(it) }
        dailyStepsData.value?.let { correctiveActivitiesList.add(it) }
        mealWindowData.value?.let { correctiveActivitiesList.add(it) }
        workoutData.value?.let { correctiveActivitiesList.add(it) }
        caffeineWindowData.value?.let { correctiveActivitiesList.add(it) }

        correctiveActivitiesListData.postValue(correctiveActivitiesList)
    }

    fun formatSecondsToHHMM(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return String.format("%02d:%02d", hours, minutes)
    }

    fun postLogData(key: String?, isLogged: Boolean?) {
        viewModelScope.launch {
            if(!key.isNullOrEmpty() && isLogged != null) {
                val reqData = JsonObject().apply {
                    addProperty("date", "")
                    addProperty(key, isLogged)
                }

                userRepository.submitLogCircadianData(reqData).collect {
                    when(it){
                        is Resource.GenericError -> {}
                        is Resource.Loading -> {}
                        is Resource.NetworkError -> {}
                        is Resource.Success<*> -> {
                            when(key){
                                light_exposure_key -> {

                                }
                                meal_window_key -> {

                                }
                                caffeine_window_key -> {

                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class CorrectiveActivitiesEnum {
    LIGHT_EXPOSURE, DAILY_STEPS, MEAL_WINDOW, WORKOUT, CAFFEINE
}