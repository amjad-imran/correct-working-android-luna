package com.oreo.ui.circadianAlignment

import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CircadianAlignmentViewModel @Inject constructor(

): BaseViewModel() {



}

enum class CorrectiveActivitiesEnum{
    LIGHT_EXPOSURE, DAILY_STEPS, MEAL_WINDOW, WORKOUT, CAFFEINE
}