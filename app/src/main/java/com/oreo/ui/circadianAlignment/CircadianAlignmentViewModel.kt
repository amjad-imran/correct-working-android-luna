package com.oreo.ui.circadianAlignment

import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.CircadianMidPointModel
import com.oreo.data.model.CircadianMidPointState
import com.oreo.data.model.CircadianMidPointStatus
import com.oreo.data.model.CorrectiveActivitiesModel
import com.oreo.data.model.OnlyImgWithText
import com.oreo.data.model.ProgressBarLytData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
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

    fun getMidPointIndex(startDateTime: LocalDateTime, currentDateTime: LocalDateTime): Int {
        val intervalSize = 10
        val circadianMinutesBetween = ChronoUnit.MINUTES.between(startDateTime, currentDateTime)
        if (circadianMinutesBetween < 0) {
            return -1
        }
        return (circadianMinutesBetween / intervalSize).toInt() //+ 1
    }

    fun whiteMidPoint(text: String): CircadianMidPointModel {
        return createMidPoint(
            index = 0,
            "#D2D2D2",
            "#3D3F43",
            text
        )
    }

    fun redMidPoint(text: String): CircadianMidPointModel {
        return createMidPoint(
            index = 0,
            "#FF8A8A",
            "#2C1F1F",
            text
        )
    }

    fun orangeMidPoint(text: String): CircadianMidPointModel {
        return createMidPoint(
            index = 0,
            "#FFB963",
            "#3C372A",
            text
        )
    }

    fun greenMidPoint(text: String): CircadianMidPointModel {
        return createMidPoint(
            index = 0,
            "#59E1A5",
            "#2A3C2D",
            text
        )
    }

    fun createMidPoint(
        index: Int,
        colorHex: String,
        bgColorHex: String,
        title: String
    ): CircadianMidPointModel {
        return CircadianMidPointModel().apply {
            this.index = index
            this.color = colorHex.toColorInt()
            this.bgColor = bgColorHex.toColorInt()
            this.title = title
        }
    }

     fun phaseState(
        bgRange: IntArray,
        phaseRange: IntArray,
        avgBeforeIndex: Int,
        avgNowIndex: Int
    ): Pair<CircadianMidPointState, CircadianMidPointStatus> {

        if (avgBeforeIndex == Int.MIN_VALUE) {
            return Pair(CircadianMidPointState.None, CircadianMidPointStatus.Locked)
        }

        if (avgNowIndex == Int.MIN_VALUE) {
            return Pair(CircadianMidPointState.None, CircadianMidPointStatus.AwaitingSync)
        }

        if (avgNowIndex == avgBeforeIndex) {
            return Pair(CircadianMidPointState.PhaseAligned, CircadianMidPointStatus.Maintained)
        }
        print("1")
        val hasAvgBeforeInBgRange = bgRange.find { it == avgBeforeIndex }
        val hasAvgNowInBgRange = bgRange.find { it == avgNowIndex }

        if (hasAvgBeforeInBgRange == null && hasAvgNowInBgRange == null) {
            return if (phaseRange.first() > avgBeforeIndex && phaseRange.first() > avgNowIndex) {
                if (avgNowIndex < avgBeforeIndex) {
                    Pair(CircadianMidPointState.PhaseAdvance, CircadianMidPointStatus.Worsening)
                } else {
                    Pair(CircadianMidPointState.PhaseAdvance, CircadianMidPointStatus.Correcting)
                }
            } else {
                if (avgNowIndex < avgBeforeIndex) {
                    Pair(CircadianMidPointState.PhaseDelay, CircadianMidPointStatus.Correcting)
                } else {
                    Pair(CircadianMidPointState.PhaseDelay, CircadianMidPointStatus.Worsening)
                }
            }
        }

        print("2")
        val hasAvgBeforeInRange = phaseRange.find { it == avgBeforeIndex }
        val hasAvgNowInRange = phaseRange.find { it == avgNowIndex }

        if (hasAvgNowInRange != null && hasAvgBeforeInRange != null) {
            print("123123 $avgBeforeIndex $avgNowIndex")
            return Pair(CircadianMidPointState.PhaseAligned, CircadianMidPointStatus.Maintained)
        }

        print("3")
        if (phaseRange.last() < avgBeforeIndex && phaseRange.last() < avgNowIndex) {
            return if (avgNowIndex < avgBeforeIndex) {
                Pair(CircadianMidPointState.PhaseDelay, CircadianMidPointStatus.Correcting)
            } else {
                Pair(CircadianMidPointState.PhaseDelay, CircadianMidPointStatus.Worsening)
            }
        }

        if (hasAvgNowInRange != null && phaseRange.last() < avgBeforeIndex) {
            print("4")
            return Pair(CircadianMidPointState.PhaseAligned, CircadianMidPointStatus.Correcting)
        }

        if (hasAvgBeforeInRange != null && phaseRange.first() < avgNowIndex) {
            return Pair(CircadianMidPointState.PhaseDelay, CircadianMidPointStatus.Worsening)
        }



        if (hasAvgBeforeInRange != null && phaseRange.first() > avgNowIndex) {
            return Pair(CircadianMidPointState.PhaseAdvance, CircadianMidPointStatus.Worsening)
        }

        if (phaseRange.first() > avgBeforeIndex && phaseRange.first() > avgNowIndex) {
            if (avgNowIndex > avgBeforeIndex) {
                return Pair(CircadianMidPointState.PhaseAdvance, CircadianMidPointStatus.Correcting)
            }
        }

        if (hasAvgNowInRange != null && phaseRange.first() > avgBeforeIndex) {
            return Pair(CircadianMidPointState.PhaseAligned, CircadianMidPointStatus.Correcting)
        }



        return Pair(CircadianMidPointState.None, CircadianMidPointStatus.AwaitingSync)
    }

}

enum class CorrectiveActivitiesEnum{
    LIGHT_EXPOSURE, DAILY_STEPS, MEAL_WINDOW, WORKOUT, CAFFEINE
}