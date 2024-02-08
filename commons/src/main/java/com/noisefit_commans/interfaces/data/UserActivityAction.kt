package com.noisefit_commans.interfaces.data

import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.SportsModeRequest
import com.noisefit_commans.models.WeatherData
import java.util.Calendar

sealed class UserActivityAction {

    class GetStepsData(val date: String) : UserActivityAction()
    class GetSleepData(val date: String) : UserActivityAction()
    object GetHeartRate : UserActivityAction()
    object GetBloodOxygenLevel : UserActivityAction()
    object GetBloodPressure : UserActivityAction()
    object GetStressCount : UserActivityAction()
    class SyncUserActivity(val date: String, val isRefresh: Boolean) : UserActivityAction()
    class UpdateSportsMode(val sportsModeRequest: SportsModeRequest) : UserActivityAction()
    class Refresh(val sportsModeRequest: SportsModeRequest) : UserActivityAction()
    class GetHeartHistory(val calendar: Calendar) : UserActivityAction()
    class SyncSportsActivity(val date: String) : UserActivityAction()
    class SyncAutoSportsActivity() : UserActivityAction()
    class PushGPSData(val gpsSignal: Int, val distance: Int) : UserActivityAction()
    object DisableEnableBluetooth : UserActivityAction()
    class OnMusicEventChanged(val event: String) : UserActivityAction()
    object OpenGmailApp : UserActivityAction()
    class SetSpo2Measurement(val status:Boolean) : UserActivityAction()
    class Default() : UserActivityAction()

    class SetWeatherData(val weatherDataList: List<WeatherData>) :
        UserActivityAction()
}
