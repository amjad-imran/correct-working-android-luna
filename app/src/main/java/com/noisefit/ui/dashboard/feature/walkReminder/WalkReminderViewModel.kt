package com.noisefit.ui.dashboard.feature.walkReminder

import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.WalkReminderData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class WalkReminderViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {


    var fetchData = false
    var selectedWeekArray =
        arrayListOf(true, true, true, true, true, true, true)
    var walkReminderData = WalkReminderData()

    private var _showEditLayout = MutableLiveData(false)
    var showEditLayout = _showEditLayout


    private var connectedDevice: ColorFitDevice? = null

    init {
        walkReminderData = WalkReminderData(false, 50, 10, 0, 22, 0, 0, null)
        connectedDevice = sessionManager.connectedDevice.value!!
        getDeviceDefaultValues()
    }

    private fun getDeviceDefaultValues() {
        _showEditLayout.value = watchesSDK.getWatchType(connectedDevice) == SDKWatchType.SDK_QUBE
    }


    fun setWalkReminder(data: WalkReminderData) {
        walkReminderData.status = data.status
        walkReminderData.goalSteps = data.goalSteps

        walkReminderData.startHour = data.startHour
        walkReminderData.startMinute = data.startMinute

        walkReminderData.endHour = data.endHour
        walkReminderData.endMinute = data.endMinute

        walkReminderData.weeks = data.weeks
        walkReminderData.repeat = data.repeat
    }


    fun generateDeviceReminderUpdateData(status: Boolean, week: IntArray): WalkReminderData {
        return WalkReminderData(
            startHour = walkReminderData.startHour,
            startMinute = walkReminderData.startMinute,
            endHour = walkReminderData.endHour,
            endMinute = walkReminderData.endMinute,
            status = status,
            goalSteps = walkReminderData.goalSteps,
            weeks = week,
            repeat = walkReminderData.repeat
        )
    }

//    fun getRepeatDayList(): ArrayList<Boolean> {
//        return walkReminderData.repeatDays?.let { ArrayList(it) } ?: ArrayList()
//    }
}
