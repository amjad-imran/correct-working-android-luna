package com.noisefit.ui.dashboard.feature.alarm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.AlarmsList
import com.noisefit_commans.models.ColorFitDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MyAlarmViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {

    var syncWithDevice = false
    private val _editMode = MutableLiveData<Boolean>()
    val editMode: LiveData<Boolean>
        get() = _editMode

    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }

    private val _mAlarmLiveData = MutableLiveData<ArrayList<AlarmsList.Alarm>>()
    var mAlarmLiveData = _mAlarmLiveData

    private val _disableDeleteOption = MutableLiveData(false)
    var disableDeleteOption = _disableDeleteOption

    var mSelectedPosition = -1

    fun setAlarmList(data: ArrayList<AlarmsList.Alarm>) {
        _mAlarmLiveData.value = ArrayList()
        _mAlarmLiveData.value?.clear()
        _mAlarmLiveData.value = data
    }

    fun updateData(data: AlarmsList.Alarm) {
        if (mSelectedPosition == -1) {
            if (_mAlarmLiveData.value == null) {
                _mAlarmLiveData.value = ArrayList()
            }
            _mAlarmLiveData.value!!.add(data)
        } else {
            if (_mAlarmLiveData.value == null) {
                _mAlarmLiveData.value = ArrayList()
            }
            _mAlarmLiveData.value?.removeAt(mSelectedPosition)
            _mAlarmLiveData.value?.add(mSelectedPosition, data)
        }

        _mAlarmLiveData.postValue(_mAlarmLiveData.value)

    }

    fun getAlarmList(): ArrayList<AlarmsList.Alarm> {
        return _mAlarmLiveData.value ?: ArrayList()
    }

    private var connectedDevice: ColorFitDevice? = null

    init {
        connectedDevice = getConnectedDevice()
        getDeviceDefaultValues()
    }

    private fun getDeviceDefaultValues() {
        _disableDeleteOption.value = watchesSDK.getWatchType(connectedDevice) == SDKWatchType.SDK_QUBE

    }

//    fun getMyReminder(): ArrayList<Reminder> {
//        val myReminderList = ArrayList<Reminder>()
//        _mAlarmLiveData.value?.forEach { reminder ->
//            myReminderList.add(
//                Reminder(
//                    reminder.id,
//                    reminder.repeatMode,
//                    reminder.repeatDays,
//                    reminder.hour,
//                    reminder.minute,
//                    reminder.label,
//                    reminder.year,
//                    reminder.month,
//                    reminder.day
//                )
//            )
//        }
//        return myReminderList
//    }

    fun getConnectedDevice(): ColorFitDevice? {
        return localDataStore.getConnectedDevice()
    }

}