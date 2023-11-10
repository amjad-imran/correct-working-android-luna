package com.noisefit.oreo

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OreoMainViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val ringDataStore: RingDataStore
) : BaseViewModel() {


    var checkBluetooth = MutableLiveData<Event<Boolean>>()


    //For API
    var selectedMasterDate: String? = null
    //Currently highlighted date
    var selectedDate: String? = null


    var bottomNavigation = MutableLiveData<Event<BottomNavOption>>()
    fun navigateTo(option: BottomNavOption) {
        bottomNavigation.postValue(Event(option))
    }

    init {
        selectedMasterDate = DateFormats.getCurrentDateOreoFormat()
        selectedDate = DateFormats.getCurrentDateOreoFormat()
    }


    val stateConnectHelp = MutableLiveData<Boolean>()
    var isHelpWidgetShown = false
    var timer: CountDownTimer? = null

    fun startDisconnectTimer() {
        if (timer == null && !isHelpWidgetShown) {
            timer = object : CountDownTimer(60000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    LOGS.w("TIMER running $millisUntilFinished")
                }

                override fun onFinish() {
                    timer = null
                    if (!isHelpWidgetShown) {
                        isHelpWidgetShown = true
                        stateConnectHelp.postValue(true)
                    }
                }
            }
            timer?.start()
        }

    }

    fun onRingConnected() {
        timer?.cancel()
        timer = null
        stateConnectHelp.postValue(false)
    }

}