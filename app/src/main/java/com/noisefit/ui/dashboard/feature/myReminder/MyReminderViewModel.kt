package com.noisefit.ui.dashboard.feature.myReminder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.Reminder
import com.noisefit_commans.models.ReminderList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyReminderViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface
) : BaseViewModel() {

    var syncWithDevice = false
    private val _editMode = MutableLiveData<Boolean>()
    val editMode: LiveData<Boolean>
        get() = _editMode

    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }

    private val _mReminderLiveData = MutableLiveData<ArrayList<ReminderList.Reminder>>()
    var mReminderLiveData = _mReminderLiveData

    var mSelectedPosition = -1

    fun setReminderList(data: ArrayList<ReminderList.Reminder>) {
        _mReminderLiveData.value = ArrayList()
        _mReminderLiveData.value?.clear()
        _mReminderLiveData.value = data
    }

    fun updateData(data: ReminderList.Reminder) {

        if (mSelectedPosition == -1) {
            if(_mReminderLiveData.value == null){
                _mReminderLiveData.value = ArrayList()
            }
            _mReminderLiveData.value!!.add(data)
        } else {
            if(_mReminderLiveData.value == null){
                _mReminderLiveData.value = ArrayList()
            }
            _mReminderLiveData.value!!.removeAt(mSelectedPosition)
            _mReminderLiveData.value!!.add(mSelectedPosition, data)
        }

        _mReminderLiveData.postValue(_mReminderLiveData.value)

    }

    fun getReminderList(): ArrayList<ReminderList.Reminder> {
        return _mReminderLiveData.value ?: ArrayList()
    }

    fun getMyReminder(): ArrayList<Reminder> {
        val myReminderList = ArrayList<Reminder>()
        _mReminderLiveData.value?.forEach { reminder ->
            myReminderList.add(
                Reminder(
                    reminder.id,
                    reminder.repeatMode,
                    reminder.repeatDays,
                    reminder.hour,
                    reminder.minute,
                    reminder.label,
                    reminder.year,
                    reminder.month,
                    reminder.day
                )
            )
        }
        return myReminderList
    }

    fun getConnectedDevice(): ColorFitDevice? {
        return localDataStore.getConnectedDevice()
    }

}