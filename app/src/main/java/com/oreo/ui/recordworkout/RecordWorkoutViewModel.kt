package com.oreo.ui.recordworkout

import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@HiltViewModel
class RecordWorkoutViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val watchDataStore: WatchDataStore,
    val ringDataStore: RingDataStore
) : BaseViewModel() {


    var gpsRequired: Boolean = false

    val showWorkoutStoppedByRingDialog = MutableLiveData<Event<Boolean>>()
    var markedDeleted: Boolean = false
    var workout: OWorkoutListModal? = null
    var sportStartTime = 0L
    var workoutDuration = 0L
    var timer: Timer? = null

    var displayTimer = MutableLiveData<String>()

    /**
     * 0->Default
     * 1->Started
     * 2->Pause
     * 3->Resume
     * 4->Stop
     */
    var currentWorkoutState = 0


    fun getCurrentTimeStamp(): Long {
        return System.currentTimeMillis() / 1000
    }

    fun updateTimer() {
        val hours = workoutDuration / 3600
        val minutes = (workoutDuration % 3600) / 60
        val seconds = workoutDuration % 60

        val timeString = if (hours == 0L) {
            String.format("%02d:%02d", minutes, seconds)
        } else {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
        displayTimer.postValue(timeString)
    }

    fun starTimer() {
        timer?.cancel()
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    workoutDuration += 1
                    updateTimer()
                }
            }, 0, 1000)
        }
    }

    fun pauseTimer() {
        timer?.cancel()
    }

    fun resumeTimer() {
        starTimer()
    }

    fun stopTimer() {
        timer?.cancel()
    }


    fun isDeviceConnected(): Boolean {
        if (isDevicePaired() == null) {
            return false
        }

        if (sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
            return true
        }
        return false
    }

    fun isDevicePaired(): ColorFitDevice? {
        return ringDataStore.getRingDevice()
    }

    fun markForDelete(sportStartTime: Long) {
        ringDataStore.addToRecordDeleteList(sportStartTime * 1000L)
    }

    fun saveOngoingRecordWorkout() {
        if (sportStartTime != 0L && workout != null) {
            ringDataStore.saveOngoingRecordWorkout(Pair(sportStartTime, workout!!))
        }
    }

    fun deleteOngoingRecordWorkout() {
        ringDataStore.deleteOngoingRecordWorkout()
    }

    fun requireGpsPermission(ringId: Int?): Boolean {
        return ringId == 207/*Outdoor running*/ || ringId == 210/*Outdoor cycling*/
    }


}