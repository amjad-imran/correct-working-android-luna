package com.noisefit.ui.dashboard.feature.myReminder

import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.Reminder
import com.noisefit_commans.models.ReminderList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AddReminderViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface
) : BaseViewModel() {

    private var _showRepeatLayout = MutableLiveData(RepeatLayoutType.NONE)
    var showRepeatLayout = _showRepeatLayout

    var reminderHour = 10
    var reminderMinute = 0
    var reminderDay = 1
    var reminderMonth = 1
    var reminderYear = 2021

    var myReminderList = ArrayList<Reminder>()
    var selectedRepeatValue = "Never"

    var selectedWeekArray =
        arrayListOf(true, true, true, true, true, true, true)


    var reminder: ReminderList.Reminder? = null

    init {
        getRepeatLayoutType()
    }

    private fun getRepeatLayoutType() {
        sessionManager.connectedDevice.value?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_NAV.deviceType, true) ||
                it.equals(DeviceType.NOISEFIT_HYBRID.deviceType, true)
            ) {
                _showRepeatLayout.value = RepeatLayoutType.LIST

            } else if (it.equals(DeviceType.COLORFIT_VISION.deviceType, true)) {
                _showRepeatLayout.value = RepeatLayoutType.WEEK

            }
        }
    }

    fun checkReminderExist(): Boolean {
        myReminderList.forEach { reminder ->
            if (reminder.day == reminderDay &&
                reminder.year == reminderYear &&
                reminder.minute == reminderMinute &&
                reminder.hour == reminderHour &&
                reminder.minute == reminderMinute
            ) {
                return true
            }
        }
        return false
    }

}

enum class RepeatLayoutType {
    LIST,
    WEEK,
    NONE
}