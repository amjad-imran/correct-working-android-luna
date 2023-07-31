package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName


data class VerifyOtpResponse(val message : String,@SerializedName("token") val authToken : String)