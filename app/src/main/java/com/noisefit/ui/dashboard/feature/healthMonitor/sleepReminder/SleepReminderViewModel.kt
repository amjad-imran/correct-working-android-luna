package com.noisefit.ui.dashboard.feature.healthMonitor.sleepReminder

import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.SleepReminder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SleepReminderViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {

    var sleepReminder = SleepReminder(false, 10, 0, 0, 0)



}