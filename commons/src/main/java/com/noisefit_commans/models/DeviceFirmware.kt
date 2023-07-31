package com.noisefit_commans.models

import com.google.gson.annotations.SerializedName


data class DeviceFirmware(@SerializedName("status") val status : String? = null,
                          @SerializedName("version") var version : String? = null,
                          @SerializedName("is_progress_available") val isProgressAvailable : Boolean? = false,
                          @SerializedName("percentage") val percentage : Int? = null,
                          @SerializedName("data") val data : String? = null,
                          @SerializedName("message") var message : String? = null) : ColorfitData()