package com.noisefit.ui.dashboard.feature.healthMonitor.heartRate

import androidx.lifecycle.MutableLiveData
import com.noisefit.data.local.AppStaticData
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.HeartRateAlert
import com.noisefit_commans.models.HeartRateInterval
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HeartRateSettingsViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {


    var hrSwitchEnabled = false
    var frequency = 5

    var alertValue = 140

    var lowHrValue = 40
    var highHrValue = 220

    var startHour = 0
    var startMinute = 0

    var endHour = 23
    var endMinute = 59


    var fetchData = false
    var fetchData2 = false
    var freqValueList: Array<String>? = null
    var lowHrValueList: Array<String>? = null
    var highHrValueList: Array<String>? = null
    var hrValueList: Array<String>? = null

    private var connectedDevice: ColorFitDevice? = null
    private var _showRealTimeHrLayout = MutableLiveData(false)
    var showRealTimeHrLayout = _showRealTimeHrLayout

    private var _showLowHrAlertLayout = MutableLiveData(true)
    var showLowHrAlertLayout = _showLowHrAlertLayout


    private var _showHrSettingLayout = MutableLiveData(false)
    var showHrSettingLayout = _showHrSettingLayout

    private var _showOnlyAlertLayout = MutableLiveData(false)
    var showOnlyAlertLayout = _showOnlyAlertLayout


    private var _showStartTimeLayout = MutableLiveData<Boolean>(true)
    var showStartTimeLayout = _showStartTimeLayout

    private var _showEndTimeLayout = MutableLiveData<Boolean>(true)
    var showEndTimeLayout = _showEndTimeLayout

    init {
        connectedDevice = sessionManager.connectedDevice.value!!
        getDeviceDefaultValues()

    }

    private fun getDeviceDefaultValues() {
        freqValueList = AppStaticData.getAutoHrTimeFrequency()
        lowHrValueList = AppStaticData.getLowHrValues()
        highHrValueList = AppStaticData.getAutoHrFrequency(isEvolve2 = false, isUltra2LE = false)
        when (connectedDevice!!.deviceType) {
            DeviceType.NOISEFIT_ACTIVE.deviceType,
            DeviceType.NOISEFIT_AGILE.deviceType -> {
                _showRealTimeHrLayout.value = true
                hrValueList =
                    AppStaticData.getAutoHrFrequency(isEvolve2 = false, isUltra2LE = false)
            }
            DeviceType.COLORFIT_ULTRA_2_LITE.deviceType,
            DeviceType.NOISEFIT_TWIST.deviceType,
            DeviceType.NOISEFIT_CURVE.deviceType,
            DeviceType.NOISEFIT_ARC.deviceType,
            DeviceType.NOISEFIT_HALO.deviceType,
            DeviceType.NOISEFIT_ORIGIN.deviceType,
            DeviceType.NOISEFIT_EVOLVE_4.deviceType,
            DeviceType.NOISEFIT_HALO_PLUS.deviceType,
            DeviceType.COLORFIT_PRO_4.deviceType,
            DeviceType.COLORFIT_PULSE_3.deviceType,
            DeviceType.COLORFIT_PRIMUS.deviceType,
            DeviceType.COLORFIT_PRO_4_GPS.deviceType,
            DeviceType.COLORFIT_PRO_4_ALPHA.deviceType,
            DeviceType.NOISEFIT_QUAD_CALL_MAX.deviceType,
            DeviceType.VISION_2_BUZZ.deviceType,
            DeviceType.COLORFIT_PULSE_2_MAX.deviceType,
            DeviceType.COLORFIT_LOOP.deviceType,
            DeviceType.COLORFIT_VICTOR.deviceType,
            DeviceType.COLORFIT_CALIBER_2.deviceType,
            DeviceType.COLORFIT_CALIBER_2_BUZZ.deviceType,
            DeviceType.NOISEFIT_EVOLVE_3.deviceType,
            DeviceType.NOISEFIT_FUSE_PLUS.deviceType,
            DeviceType.NOISEFIT_ARC_PLUS.deviceType,
            DeviceType.NOISEFIT_FUSE.deviceType,
            DeviceType.NOISEFIT_VORTEX.deviceType,
            DeviceType.NOISEFIT_FORCE_PLUS.deviceType,
            DeviceType.PULSE_GO_BUZZ.deviceType,
            DeviceType.COLORFIT_CALIBER_BUZZ.deviceType,
            DeviceType.COLORFIT_ULTRA_2_BUZZ.deviceType,
            DeviceType.COLORFIT_CALIBER_GO.deviceType,
            DeviceType.COLORFIT_VISION_2.deviceType,
            DeviceType.NOISEFIT_CREW.deviceType,
            DeviceType.NOISEFIT_METTLE.deviceType,
            DeviceType.NOISEFIT_CREW_PRO.deviceType,
            DeviceType.NOISEFIT_TWIST_PRO.deviceType,
            DeviceType.NOISEFIT_METALLIX.deviceType,
            DeviceType.COLORFIT_VISION_3.deviceType,
            DeviceType.ULTRA_3.deviceType,
            DeviceType.COLORFIT_ORE.deviceType,
            DeviceType.COLORFIT_PRO_5_47MM.deviceType,
            DeviceType.COLORFIT_PRO_5_44MM.deviceType,
            DeviceType.NOISEFIT_ACTIVE_2.deviceType,
            DeviceType.COLORFIT_CHROME.deviceType,
            DeviceType.NOISEFIT_ENDEAVOUR.deviceType -> {
                _showOnlyAlertLayout.value = true
                hrValueList = AppStaticData.getAutoHrFrequency(isEvolve2 = false, isUltra2LE = true)
            }
            DeviceType.NOISE_EVOLVE_2.deviceType,
            DeviceType.COLORFIT_PULSE_2.deviceType,
            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType,
            DeviceType.NOISE_EVOLVE_2_PLAY.deviceType -> {
                _showLowHrAlertLayout.value = false
                highHrValueList =
                    AppStaticData.getAutoHrFrequency(isEvolve2 = true, isUltra2LE = false)
                freqValueList = AppStaticData.getEvolveAutoHrTimeFrequency()
                _showHrSettingLayout.value = true
                hrValueList = AppStaticData.getAutoHrFrequency(isEvolve2 = true, isUltra2LE = false)
            }
            DeviceType.COLORFIT_NAV.deviceType,
            DeviceType.NOISEFIT_HYBRID.deviceType,
            DeviceType.NOISEFIT_ENDURE.deviceType -> {
                hrValueList =
                    AppStaticData.getAutoHrFrequency(isEvolve2 = false, isUltra2LE = false)
                _showHrSettingLayout.value = true
            }
            DeviceType.COLORFIT_VISION.deviceType, -> {
                hrValueList =
                    AppStaticData.getAutoHrFrequency(isEvolve2 = false, isUltra2LE = false)
                _showHrSettingLayout.value = true
                _showEndTimeLayout.value = false
                _showStartTimeLayout.value = false
            }
            DeviceType.COLORFIT_PRO_3.deviceType,
            DeviceType.COLORFIT_PRO_2.deviceType,
            DeviceType.COLORFIT_PRO_2_OXY.deviceType -> {
                _showRealTimeHrLayout.value = false
                hrValueList =
                    AppStaticData.getAutoHrFrequency(isEvolve2 = false, isUltra2LE = false)
            }
            DeviceType.COLORFIT_NAV_PLUS.deviceType,
            DeviceType.COLORFIT_2.deviceType -> {
                hrValueList =
                    AppStaticData.getAutoHrFrequency(isEvolve2 = false, isUltra2LE = false)
            }
            DeviceType.COLORFIT_MIGHTY.deviceType,
            DeviceType.NOISEFIT_NOVA.deviceType -> {
                hrValueList = Array(7) { "${140 + (it * 10)} BPM" }
                _showOnlyAlertLayout.value = true
            }
            else -> {
                hrValueList =
                    AppStaticData.getAutoHrFrequency(isEvolve2 = false, isUltra2LE = false)
                _showOnlyAlertLayout.value = true

            }
        }
    }

    /*
      sessionManager.connectedDevice.value?.let {
            if(it.deviceType.equals(DeviceType.COLORFIT_NAV_PLUS.deviceType,true) ||
                it.deviceType.equals(DeviceType.COLORFIT_2.deviceType,true)){
                binding.textView11.gone()
                binding.tvAlertFrequency.gone()
                binding.view4.gone()
            }
        }
     */

    fun getHrValue(): String {
        return "$alertValue BPM"
    }

    fun getFreqValue(): String {
        return "$frequency Mins"
    }

    fun getLowHrValue(): String {
        return "$lowHrValue BPM"
    }

    fun getHighHrValue(): String {
        return "$highHrValue BPM"
    }


    fun getHeartRateInterval(): HeartRateInterval {
        return HeartRateInterval(
            true,
            frequency,
            "${startHour}:${startMinute}",
            "${endHour}:${endMinute}",
            true
        )
    }

    fun getHeartRateAlert(): HeartRateAlert {
        return HeartRateAlert(
            true,
            lowHrValue,
            highHrValue
        )
    }

    fun updateHRInterval(heartRateInterval: HeartRateInterval) {
        hrSwitchEnabled = heartRateInterval.status
        val startTimeSplit = heartRateInterval.startTime.split(":")
        if (startTimeSplit.size == 2) {
            startHour = startTimeSplit[0].toInt()
            startMinute = startTimeSplit[1].toInt()
        }
        val endTimeSplit = heartRateInterval.endTime.split(":")
        if (endTimeSplit.size == 2) {
            endHour = endTimeSplit[0].toInt()
            endMinute = endTimeSplit[1].toInt()
        }

        frequency = heartRateInterval.interval
    }

    fun updateHRAlert(heartRateAlert: HeartRateAlert) {
        lowHrValue = heartRateAlert.min_hr
        highHrValue = heartRateAlert.max_hr

    }
}