package com.noisefit.oreo

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.UserHealthData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OreoMainViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val ringDataStore: RingDataStore,
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {


    var checkBluetooth = MutableLiveData<Event<Boolean>>()

    //For API
    var selectedMasterDate: String? = null

    //Currently highlighted date
    var selectedDate: String? = null

    val userHealthData = ArrayList<UserHealthData>()

    var bottomNavigation = MutableLiveData<Event<BottomNavOption>>()
    fun navigateTo(option: BottomNavOption) {
        bottomNavigation.postValue(Event(option))
    }

    init {
        selectedMasterDate = DateFormats.getCurrentDateOreoFormat()
        selectedDate = DateFormats.getCurrentDateOreoFormat()

        getUserHealthData(selectedMasterDate)
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

    fun getUserHealthData(selectedMasterDate: String?) {
        viewModelScope.launch {
            userActivityRepository.getUserHealthData(
                selectedMasterDate ?: DateFormats.getCurrentDateOreoFormat()
            ).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getUserHealthData(selectedMasterDate)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            userHealthData.add(UserHealthData(date = "2023-11-03"))
                            userHealthData.add(UserHealthData(date = "2023-11-04"))
                            userHealthData.add(UserHealthData(date = "2023-11-05"))
                            userHealthData.add(UserHealthData(date = "2023-11-06"))
                            userHealthData.add(UserHealthData(date = "2023-11-07"))
                            userHealthData.add(UserHealthData(date = "2023-11-17"))
                            LOGS.w("GOT Result")

                        }
                    }
                }
            }
        }


    }

    fun shouldLoadMoreData(): Boolean {
        if (userHealthData.isEmpty()) return true

        if ((userHealthData[0]).date.equals(selectedDate)) {
            return true
        }

        val isTodayDate = selectedDate.equals(DateFormats.getDate(DateFormats.dateFormat3))

        if (isTodayDate) return false

        if ((userHealthData[userHealthData.size - 1]).date.equals(selectedDate)) {
            return true
        }


        return false
    }


}