package com.noisefit_commans.interfaces.data

import com.noisefit_commans.interfaces.base.BaseActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.SportsModeRequest
import com.noisefit_commans.models.WeatherData
import java.util.Calendar

abstract class UserActivityDataActions() :
    BaseActions() {

    abstract fun getStepsData(date: String)
    abstract fun getSleepData(date: String)
    abstract fun getHeartRate()
    abstract fun getBloodOxygenLevel()
    abstract fun getBloodPressure()
    abstract fun getStressCount()
    abstract fun getBodyTemperatureData()
    open fun syncUserActivity(date: String, isRefresh: Boolean) {}
    open fun syncUserActivityByMode(date: String, mode: Int?) {
        syncUserActivity(date, true)
    }
    open fun updateSportsMode(sportsModeRequest: SportsModeRequest) {}
    open fun refresh(sportsModeRequest: SportsModeRequest) {}
    open fun getHeartHistory(calendar: Calendar) {}
    open fun syncSportsActivity(date: String) {}
    open fun syncAutoSports() {}
    open fun pushGPSData(gpsSignal: Int, distance: Int) {}
    open fun disableEnableBluetooth() {}
    open fun onMusicEventChanged(event: String) {
        //  MusicPlayerControlsHandler.onEvent(event)
    }

    open fun setWeatherData( weatherDataList: List<WeatherData>) {}
    open fun setSpo2MeasurementData(status: Boolean){}


    open fun openGmailApp() {}
    abstract fun setDevice(colorFitDevice: ColorFitDevice)
}
