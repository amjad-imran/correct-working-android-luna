package com.oreo.data.model

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.noisefit_commans.models.*
import com.oreo.data.db.database.OreoDayTimeMovementDao
import com.oreo.data.db.database.OreoRespiratoryDao

data class OreoUserSyncActivities(
    @SerializedName("sleepData") var sleepData: List<OreoSleepData>? = null,
    @SerializedName("hrHistoryData") var hrHistoryData: List<OreoHeartRate>? = null,
    @SerializedName("bodyTemperatureData") var bodyTemperature: List<OreoBodyTemperatureBreakup>? = null,
    @SerializedName("boData") var boData: List<OreoBloodOxygenBreakup>? = null,
    @SerializedName("stressData") var stressData: List<OreoStressDataBreakup>? = null,
    @SerializedName("dayTimeMovement") var dayTimeMovement: List<DayTimeMovementBreakup>? = null,
    @SerializedName("respiratory") var respiratory: List<OreoRespiratoryData>? = null,
    @SerializedName("synced") var synced: Boolean? = false,
    @SerializedName("stepsDataList") var stepsDataList: List<OreoStepsData>? = null,
)

data class OreoUserSyncRawData(
    @SerializedName("sleepData") var sleepData: List<OreoSleepData>? = null,
    @SerializedName("hrHistoryData") var hrHistoryData: List<OreoHeartRate>? = null,
    @SerializedName("bodyTemperatureData") var bodyTemperature: List<OreoBodyTemperatureBreakup>? = null,
    @SerializedName("boData") var boData: List<OreoBloodOxygenBreakup>? = null,
    @SerializedName("stressData") var stressData: List<OreoStressDataBreakup>? = null,
    @SerializedName("respiratory") var respiratory: List<OreoRespiratoryData>? = null,
    @SerializedName("dayTimeMovement") var dayTimeMovement: List<DayTimeMovementBreakup>? = null,
    @SerializedName("stepsDataList") var stepsDataList: List<OreoStepsData>? = null,
)