package com.noisefit_commans.data.model.caffeine

import com.google.gson.annotations.SerializedName

data class CaffeineGraphDataModel(
    @SerializedName("wake_time")
    val wakeUpTime: String,//HH:mm:ss
    val status: Boolean?=null,
    @SerializedName("bed_time")
    val bedTime: String,//HH:mm:ss
    @SerializedName("caffeine_start")
    val caffeineStartTime: String,//HH:mm:ss
    @SerializedName("caffeine_end")
    val caffeineEndTime: String,//HH:mm:ss
//    val caffeineValues: List<Int>,//HH:mm:ss
    val caffeine_window: List<CaffeineWindowData>,

    val title: String,
    val message: String,

)

data class CaffeineWindowData(
    val time: String,// HH:mm:ss
    val dose: Int // 400
)
