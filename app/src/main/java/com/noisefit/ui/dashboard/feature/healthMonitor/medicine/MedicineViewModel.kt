package com.noisefit.ui.dashboard.feature.healthMonitor.medicine

import androidx.lifecycle.MutableLiveData
import com.noisefit.data.local.AppStaticData
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.dashboard.feature.idle.FrequencyIn
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.SedentaryData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MedicineViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {

    var firstTimeData = false
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
        _frequencyList.value = AppStaticData.getMedicineReminderFrequencyValues()
        _frequencyIn.value = FrequencyIn.HOUR
//        when (connectedDevice!!.deviceType) {
//            DeviceType.COLORFIT_NAV.deviceType,
//            DeviceType.COLORFIT_VISION.deviceType,
//            DeviceType.NOISEFIT_HYBRID.deviceType -> {
//                showRepeatLayout.value = true
//                _frequencyList.value = AppStaticData.getIdleAlertFrequencyValues()
//                _frequencyIn.value = FrequencyIn.MINUTE
//            }
//            DeviceType.NOISEFIT_ENDURE.deviceType -> {
//                _frequencyList.value = AppStaticData.getIdleAlertFrequencyValues()
//                _frequencyIn.value = FrequencyIn.MINUTE
//            }
//            DeviceType.NOISE_EVOLVE_2.deviceType -> {
//                _frequencyIn.value = FrequencyIn.NONE
//            }
//            else -> {
//
//                _frequencyIn.value = FrequencyIn.HOUR
//            }
//        }
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
