package com.noisefit_commans.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ScannedDevice(val deviceName : String?, val deviceMac :String,val rssi : Int=0) : Parcelable
