package com.noisefit_commans.interfaces.data

import com.noisefit_commans.constants.SyncEvents
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.noisefit_commans.data.model.RecordedWorkoutData
import com.noisefit_commans.models.BloodOxygenBreakup
import com.noisefit_commans.models.BloodPressureData
import com.noisefit_commans.models.BodyTemperatureBreakup
import com.noisefit_commans.models.HeartRate
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SportsModeListGPS
import com.noisefit_commans.models.SportsModeRequestList
import com.noisefit_commans.models.StepsData
import com.noisefit_commans.models.StressDataBreakup

sealed class UserActivityCallback {

    class StepsDataObtained(val stepsData: StepsData) : UserActivityCallback()
    class StepsDataObtainedOreo(val stepsData: OreoStepsData) : UserActivityCallback()

    class RealStepsDataObtained(val stepsData: StepsData) : UserActivityCallback()
    class AutoSportDataObtained(val data: List<OreoAutoSportData>) : UserActivityCallback()
    class GetTempFromLatLog(val lat: Double, val log: Double) : UserActivityCallback()
    class SleepDataObtained(val sleepData: SleepData) : UserActivityCallback()
    class SleepDataObtainedOreo(val sleepData: OreoSleepData) : UserActivityCallback()
    class NapObtainedOreo(val sleepData: OreoSleepData) : UserActivityCallback()
    class HealthScoreObtainedOreo(val score: Int, val date: String) : UserActivityCallback()

    //    class HeartRateObtained(val heartRate: HeartRate) : UserActivityCallback()
    class BloodOxygenObtained(val bloodOxygen: List<BloodOxygenBreakup>) : UserActivityCallback()
    class OreoBloodOxygenObtained(val bloodOxygen: OreoBloodOxygenBreakup) : UserActivityCallback()
    class OreoRingDayTimeMovementObtained(val dayTimeMovement: DayTimeMovementBreakup) : UserActivityCallback()
    class OreoRespiratoryDataObtained(val respiratoryData: OreoRespiratoryData) :
        UserActivityCallback()

    class BloodPressureObtained(val bloodPressureData: BloodPressureData) : UserActivityCallback()
    class StressDataObtained(val stressData: List<StressDataBreakup>) : UserActivityCallback()
    class StressDataObtainedOreo(val stressData: OreoStressDataBreakup) : UserActivityCallback()
    class BodyTemperatureObtained(val bodyTemperatureBreakupData: List<BodyTemperatureBreakup>) :
        UserActivityCallback()

    class BodyTemperatureObtainedOreo(val bodyTemperatureBreakupData: OreoBodyTemperatureBreakup) :
        UserActivityCallback()

    class UserDataSyncUpdated(val syncStatus: SyncEvents) : UserActivityCallback()

    //    class SportsModeDataObtained(val sportsModeResponse: SportsModeList) : UserActivityCallback()
    class HeartHistoryObtained(val heartRateData: List<HeartRate>) : UserActivityCallback()
    class HeartHistoryObtainedOreo(val heartRateData: OreoHeartRate) : UserActivityCallback()
//    class SportsModeDataObtainedGPS(val sportsModeResponse: SportsModeListGPS) :
//        UserActivityCallback()


    class SportsModeDataObtained(val sportsModeRequestList: SportsModeRequestList) :
        UserActivityCallback()

    class SportsModeDataObtainedGPS(val sportsModeResponse: SportsModeListGPS) :
        UserActivityCallback()

    class SportsModeDataSyncSuccess() : UserActivityCallback()
    class RingUserWorkoutData(val data:List<RecordedWorkoutData>) : UserActivityCallback()

}