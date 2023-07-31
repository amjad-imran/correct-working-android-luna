package com.noisefit.ui.dashboard.feature.drink

import androidx.lifecycle.MutableLiveData
import com.noisefit.data.local.AppStaticData
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.dashboard.feature.idle.FrequencyIn
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.SedentaryData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class DrinkReminderViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {

    private var _showRepeatLayout = MutableLiveData(false)
    var showRepeatLayout = _showRepeatLayout

    var sedentaryData = SedentaryData()

    private var _frequencyIn = MutableLiveData<FrequencyIn>()
    var frequencyIn = _frequencyIn

    private var _frequencyList = MutableLiveData<Array<String>>()
    var frequencyList = _frequencyList

    private var connectedDevice: ColorFitDevice? = null

    init {
        sedentaryData = SedentaryData(false, 0, 10, 0, 22, 0, 0, null, null)
        connectedDevice = sessionManager.connectedDevice.value!!
        getDeviceDefaultValues()
    }


    private fun getDeviceDefaultValues() {
        when (connectedDevice!!.deviceType) {
            DeviceType.COLORFIT_PRO_3.deviceType -> {
                _frequencyList.value = AppStaticData.getHandWashFrequencyValues()
                _frequencyIn.value = FrequencyIn.MINUTE
                _showRepeatLayout.value = true
            }
            DeviceType.COLORFIT_VISION.deviceType -> {
                _frequencyList.value = AppStaticData.getHandWashFrequencyValues()
                _frequencyIn.value = FrequencyIn.MINUTE
            }
            DeviceType.NOISE_EVOLVE_2.deviceType,
            DeviceType.NOISE_EVOLVE_2_PLAY.deviceType,
            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType,
            DeviceType.COLORFIT_PULSE_2.deviceType -> {
                _frequencyList.value = AppStaticData.getHandWashFrequencyValuesEvolve()
                _frequencyIn.value = FrequencyIn.MINUTE
            }
            else -> {
                _frequencyList.value = AppStaticData.getReminderFrequencyValues()
                _frequencyIn.value = FrequencyIn.HOUR
            }
        }
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

    fun getRepeatDayList(): ArrayList<Boolean> {
        return sedentaryData.repeatDays?.let { ArrayList(it) } ?: ArrayList()
    }

    fun getIntervalInMinutes(): Int {
        return when (_frequencyIn.value) {
            FrequencyIn.HOUR -> {
                sedentaryData.interval * 60
            }
            FrequencyIn.MINUTE -> {
                sedentaryData.interval
            }
            FrequencyIn.SECOND -> {
                sedentaryData.interval * 60
            }
            else -> {
                -1
            }
        }
    }


    fun setSedentary(data: SedentaryData) {
        sedentaryData.status = data.status
        sedentaryData.interval = data.interval

        sedentaryData.startHour = data.startHour
        sedentaryData.startMinute = data.startMinute

        sedentaryData.endHour = data.endHour
        sedentaryData.endMinute = data.endMinute

        sedentaryData.repeatDays = data.repeatDays
        sedentaryData.repeat = data.repeat
    }

    fun generateDeviceReminderUpdateData(status: Boolean): SedentaryData {
        return SedentaryData(
            startHour = sedentaryData.startHour,
            startMinute = sedentaryData.startMinute,
            endHour = sedentaryData.endHour,
            endMinute = sedentaryData.endMinute,
            status = status,
            interval = sedentaryData.interval,
            repeatDays = sedentaryData.repeatDays
        )
    }

}

