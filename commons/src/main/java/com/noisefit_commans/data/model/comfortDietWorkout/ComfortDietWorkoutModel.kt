package com.noisefit_commans.data.model.comfortDietWorkout

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ComfortDietWorkoutModel(
    var date: String,
    var isSetup: Boolean
) : Parcelable