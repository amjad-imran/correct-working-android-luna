package com.noisefit_commans.data.model

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.*

data class UserSyncActivities(
    @SerializedName("sleepData") var sleepData: List<SleepData>? = null,
    @SerializedName("hrHistoryData") var hrHistoryData: List<HeartRateHistory>? = null,
    @SerializedName("bodyTemperatureData") var bodyTemperature: List<BodyTemperature>? = null,
    @SerializedName("boData") var boData: List<BloodOxygen>? = null,
    @SerializedName("stressData") var stressData: List<StressData>? = null,
    @SerializedName("synced") var synced: Boolean? = false,
    @SerializedName("stepsDataList") var stepsDataList: List<StepsData>? = null,
)

data class UserSyncRawData(
    @SerializedName("sleepData") var sleepData: List<SleepData>? = null,
    @SerializedName("hrHistoryData") var hrHistoryData: List<HeartRate>? = null,
    @SerializedName("bodyTemperatureData") var bodyTemperature: List<BodyTemperatureBreakup>? = null,
    @SerializedName("boData") var boData: List<BloodOxygenBreakup>? = null,
    @SerializedName("stressData") var stressData: List<StressDataBreakup>? = null,
    @SerializedName("stepsDataList") var stepsDataList: List<StepsData>? = null,
)