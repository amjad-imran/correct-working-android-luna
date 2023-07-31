package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExperimentalSettings(var smartNotification: Boolean = false,var findMyPhoneNotification: Boolean = false) : Parcelable