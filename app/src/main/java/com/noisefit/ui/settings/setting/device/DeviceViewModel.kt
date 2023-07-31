package com.noisefit.ui.settings.setting.device

import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val dataUnitConverter: DataUnitConverter,
) : BaseViewModel() {

    var watchPassword: WatchPassword = WatchPassword(status = false)
    var brightnessLevel = 1
    var screenTime = 5
    var vibrationIntensity = "week"
    var timeFormat = TimeFormats.HOURS_12
    var tempUnit: Units = Units.METRIC
    var language: String = "English"
    var deviceLangList = ArrayList<String>()


    init {

        tempUnit = localDataStore.getBodyTempUnit()
        feedLanguage()
    }

    fun getInitialDeviceTimeFormat() {
        timeFormat = if (localDataStore.getTimeFormat()?.lowercase()
                .equals(TimeFormats.HOURS_12.type.lowercase())
        ) {
            TimeFormats.HOURS_12
        } else {
            TimeFormats.HOURS_24
        }
    }

    fun setPassword(wPassword: WatchPassword) {
        watchPassword.password = wPassword.password
        watchPassword.status = wPassword.status
    }

    private fun feedLanguage() {
        when (sessionManager.connectedDevice.value?.deviceType) {
            DeviceType.NOISE_EVOLVE_2.deviceType, DeviceType.NOISE_EVOLVE_2_PLAY.deviceType -> {
                deviceLangList.add("English")
                deviceLangList.add("Hindi")
            }
            else -> {
                deviceLangList.add("English")
                deviceLangList.add("Chinese")
            }
        }

    }

    fun getTemperatureUnit(): String {
        return when (tempUnit) {
            Units.METRIC -> {
                return "Celsius"
            }
            Units.IMPERIAL -> {
                return "Fahrenheit"
            }
            else -> ""
        }
    }


    fun getVibration(): VibrationIntensityEnum {
        return when (vibrationIntensity) {
            VibrationIntensityEnum.Weak.intensity -> {
                VibrationIntensityEnum.Weak
            }
            VibrationIntensityEnum.Medium.intensity -> {
                VibrationIntensityEnum.Medium
            }
            VibrationIntensityEnum.Strong.intensity -> {
                VibrationIntensityEnum.Strong
            }
            else -> {
                VibrationIntensityEnum.Weak
            }
        }
    }

    fun fromLanguage(): DeviceLanguage {
        return when (language.lowercase()) {
            "hindi" -> {
                return DeviceLanguage.HINDI
            }
            "chinese" -> {
                return DeviceLanguage.CHINESE
            }
            else -> DeviceLanguage.ENGLISH
        }
    }

    fun toLanguage(language: Language): String {
        val lang = language.language?.lowercase()

        this.language = if (lang.equals("hindi", true)) {
            "Hindi"
        } else if (lang.equals("chinese", true)) {
            "Chinese"
        } else {
            "English"
        }

        return this.language

    }
}