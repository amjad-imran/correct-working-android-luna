package com.noisefit_commans.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Watch (var deviceType: DeviceType,var macAddress : String?="") : Parcelable