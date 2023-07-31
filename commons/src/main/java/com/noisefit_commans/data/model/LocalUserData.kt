package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocalUserData(
    var name: String? = null,
    var endGame: String? = null,
    var gender: String? = null,
    var dob: String? = null,
    var height: Int? = null,
    var weight: Int? = null,
    var stepsGoal: Int? = null,
    var heightUnit: String? = null,
    var weightUnit: String? = null,
    var interests: List<Int>? = null,
) : Parcelable
