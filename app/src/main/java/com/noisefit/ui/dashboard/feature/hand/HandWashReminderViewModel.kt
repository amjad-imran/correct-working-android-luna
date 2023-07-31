package com.noisefit.ui.dashboard.feature.hand

import androidx.lifecycle.MutableLiveData
import com.noisefit.data.local.AppStaticData
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.dashboard.feature.idle.FrequencyIn
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.HandWashing
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HandWashReminderViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {

    var handwash = HandWashing()

    private var _showDurationLayout = MutableLiveData(false)
    var showDurationLayout = _showDurationLayout

    private var _frequencyIn = MutableLiveData<FrequencyIn>()
    var frequencyIn = _frequencyIn

    private var _frequencyList = MutableLiveData<Array<String>>()
    var frequencyList = _frequencyList

    private var connectedDevice: ColorFitDevice? = null

    init {
        handwash = HandWashing(0, 10, 0, 22, 0, 5, false)
        connectedDevice = sessionManager.connectedDevice.value!!
        getDeviceDefaultValues()
    }


    private fun getDeviceDefaultValues() {
        when (connectedDevice!!.deviceType) {
            DeviceType.COLORFIT_NAV.deviceType,
            DeviceType.COLORFIT_VISION.deviceType -> {
                _showDurationLayout.value = true
                _frequencyList.value = AppStaticData.getHandWashFrequencyValues()
                _frequencyIn.value = FrequencyIn.MINUTE
            }
            DeviceType.COLORFIT_PRO_3.deviceType -> {
                _frequencyList.value = AppStaticData.getHandWashFrequencyValues()
                _frequencyIn.value = FrequencyIn.MINUTE
            }
            else -> {
                _frequencyList.value = AppStaticData.getReminderFrequencyValues()
                _frequencyIn.value = FrequencyIn.HOUR
            }
        }
    }

    fun getDurationValue(value: Int): String {
        return "$value sec"
    }


    fun getFrequencyValue(value: Int): String {
        return when (_frequencyIn.value) {
            FrequencyIn.HOUR -> {
                "$value hr"
            }
            FrequencyIn.MINUTE -> {
                "$value min"
            }
            FrequencyIn.SECOND -> {
                "$value sec"
            }
            else -> {
                ""
            }
        }
    }

    fun setHandWash(data: HandWashing) {
        val hWash = HandWashing(
            data.startHour,
            data.startMinute,
            data.endHour,
            data.endMinute,
            data.frequency,
            data.duration,
            data.startWash
        )
        handwash = hWash
    }

    fun getIntervalInMinutes(): Int {
        val freq = handwash.frequency
        return when (_frequencyIn.value) {
            FrequencyIn.HOUR -> {
                freq * 60
            }
            FrequencyIn.MINUTE -> {
                freq
            }
            FrequencyIn.SECOND -> {
                freq * 60
            }
            else -> {
                -1
            }
        }
    }

    fun generateDeviceReminderUpdateData(status: Boolean): HandWashing {
        return HandWashing(
            startHour = handwash.startHour,
            startMinute = handwash.startMinute,
            endHour = handwash.endHour,
            endMinute = handwash.endMinute,
            startWash = status,
            duration = handwash.duration,
            frequency = handwash.frequency
        )
    }

}
