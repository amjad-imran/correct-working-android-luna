package com.noisefit_commans.data.model

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.*

data class UserActivities(
    @SerializedName("stepsData") var stepsData: StepsData? = null,
    @SerializedName("sleepData") var sleepData: SleepData? = null,
    @SerializedName("hrHistoryData") var hrHistoryData: HeartRateHistory? = null,
    @SerializedName("bodyTemperatureData") var bodyTemperature: BodyTemperature? = null,
    @SerializedName("bpData") var bpData: BloodPressureData? = null,
    @SerializedName("boData") var boData: BloodOxygen? = null,
    @SerializedName("stressData") var stressData: StressData? = null,
    @SerializedName("synced") var synced: Boolean? = false,
    @SerializedName("stepsDataList") var stepsDataList: List<StepsData>? = null,
    )