package com.oreo.ui.sleep.scoredetails

import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedOSCDViewModel @Inject constructor(val sessionManager: SessionManager) :
    BaseViewModel() {
    /*
    * 0-Day
    * 1-Week
    * 2-Month
    * */
    var selectedTab: Int = 0
    var itemType: String = ""
    var itemClickType: ViewItemClickType? = null


}

enum class ClickViewType {
    ACTIVITY_SCORE, GOAL_PROGRESS_DAY, GOAL_PROGRESS_WEEK, GOAL_PROGRESS_MONTH, TOTAL_BURN_DAY,
    TOTAL_BURN_WEEK, TOTAL_BURN_MONTH, STEP_DAY, SLEEP, ACTIVITY, READINESS, DISTANCE
}

enum class ViewItemClickType {
    SLEEP_SCORE, TOTAL_SLEEP, TIME_IN_BED, SLEEP_EFFICIENCY, RESTING_HR, READINESS_SCORE, HR_VARIABILITY, BODY_TEMPERATURE, RESPIRATORY_RATE,
    ACTIVITY_SCORE, ACTIVE_CALORIES, TOTAL_CALORIES_BURNED, STEPS, DISTANCE
}