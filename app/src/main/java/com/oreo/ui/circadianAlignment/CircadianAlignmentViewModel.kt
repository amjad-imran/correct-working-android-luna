package com.oreo.ui.circadianAlignment

import androidx.lifecycle.MutableLiveData
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.CorrectiveActivitiesModel
import com.oreo.data.model.OnlyImgWithText
import com.oreo.data.model.ProgressBarLytData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CircadianAlignmentViewModel @Inject constructor(
    val resourceProvider: ResourcesProvider,
): BaseViewModel() {

    val lightExposureData = MutableLiveData<CorrectiveActivitiesModel>()
    val dailyStepsData = MutableLiveData<CorrectiveActivitiesModel>()
    val mealWindowData = MutableLiveData<CorrectiveActivitiesModel>()
    val workoutData = MutableLiveData<CorrectiveActivitiesModel>()
    val caffeineWindowData = MutableLiveData<CorrectiveActivitiesModel>()

    fun prepareCorrectiveActivitiesData(): List<CorrectiveActivitiesModel>{

        // Light Exposure Data
        lightExposureData.value = CorrectiveActivitiesModel(
            bgMainImg = R.drawable.bg_light_exposure_corrective_activities,
            title = resourceProvider.getString(R.string.text_light_exposure),
            desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
            onlyImgWithText = OnlyImgWithText(
                img = R.drawable.ic_sun_activity_monitor,
                txt = "45\nmins"
            ),
            progressBarLytData = null,
            isOpen = true,
            time = "02:31 left"
        )

        // Daily Steps Data
        dailyStepsData.value = CorrectiveActivitiesModel(
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
            isOpen = true,
            time = "02:31 left"
        )

        // Meal Window Data
        mealWindowData.value = CorrectiveActivitiesModel(
            bgMainImg = R.drawable.bg_meal_window_corrective_activities,
            title = resourceProvider.getString(R.string.text_meal_window),
            desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
            onlyImgWithText = OnlyImgWithText(
                img = R.drawable.ic_meal_corrective_activities,
                txt = null
            ),
            progressBarLytData = null,
            isOpen = true,
            time = "02:31 left"
        )

        // Workout Data
        workoutData.value = CorrectiveActivitiesModel(
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
            isOpen = true,
            time = "02:31 left"
        )

        // Caffeine Window Data
        caffeineWindowData.value = CorrectiveActivitiesModel(
            bgMainImg = R.drawable.bg_caffeine_window_corrective_activities,
            title = resourceProvider.getString(R.string.text_caffeine_window2),
            desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
            onlyImgWithText = OnlyImgWithText(
                img = R.drawable.ic_caffeine_corrective_activities,
                txt = null
            ),
            progressBarLytData = null,
            isOpen = true,
            time = "02:31 left"
        )

        val correctiveActivitiesList:
                ArrayList<CorrectiveActivitiesModel> = ArrayList()

        lightExposureData.value?.let { correctiveActivitiesList.add(it) }
        dailyStepsData.value?.let { correctiveActivitiesList.add(it) }
        mealWindowData.value?.let { correctiveActivitiesList.add(it) }
        workoutData.value?.let { correctiveActivitiesList.add(it) }
        caffeineWindowData.value?.let { correctiveActivitiesList.add(it) }

        return correctiveActivitiesList
    }

}

enum class CorrectiveActivitiesEnum{
    LIGHT_EXPOSURE, DAILY_STEPS, MEAL_WINDOW, WORKOUT, CAFFEINE
}