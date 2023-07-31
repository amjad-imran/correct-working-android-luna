package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FitnessHealthModel(var title:String="", var msg:String="", var hyperLink:String="") : Parcelable