package com.noisefit.ui.dashboard.feature.findmydevice

import androidx.lifecycle.MutableLiveData
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FindMyDeviceViewModel
@Inject
constructor(
    val watchesSDK: WatchesSDK,
    private val sessionManager: SessionManager,
) : BaseViewModel() {

    private var _vibrationState = MutableLiveData(VibrationState.None)
    var vibrationState = _vibrationState


    var sDKWatchType: SDKWatchType?=null
    fun updateState(vibrationState: VibrationState) {
        _vibrationState.postValue(vibrationState)
    }

    fun getButtonText(): Int {
        return when (_vibrationState.value) {
            VibrationState.None -> {
                R.string.text_search
            }
            VibrationState.Off -> {
                R.string.text_search_again
            }
            VibrationState.Vibrating -> {
                R.string.text_stop_searching
            }
            else -> {
                throw NullPointerException("invalid vibration state")
            }
        }
    }

    fun getSessionManager(): SessionManager {
        return sessionManager
    }
}


enum class VibrationState {
    None,
    Vibrating,
    Off
}