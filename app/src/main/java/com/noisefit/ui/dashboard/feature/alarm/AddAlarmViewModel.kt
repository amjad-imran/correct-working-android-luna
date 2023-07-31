package com.noisefit.ui.dashboard.feature.alarm

import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.AlarmsList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddAlarmViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface
) : BaseViewModel() {

    /**
     * First value for status
     */
    var selectedWeekArray = arrayListOf(true, true, true, true, true, true, true, true)
    var isAlarmUpdated = false
    var selectedAm = true
    var editAlarm: AlarmsList.Alarm? = null
     var mHour: Int = 0
     var mMinute: Int = 0
}