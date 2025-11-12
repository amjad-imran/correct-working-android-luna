package com.noisefit_commans.data.model.lifeos.onboarding

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Question(
    val answer: List<AnswerX>,
    val id: Int,
    val text: String,
    val type: String,

    // for app
    var isSavedByUser: Boolean = false
) : Parcelable

@Parcelize
data class AnswerX(
    val addOntext: String,
    val id: Int,
    val text: String,

    // for app
    var isSelected: Boolean = false,
    var userInputText: String ?= null, // used only for 'Other'
    var state: LifeOSOnboardMCQquesStates ?= null,
) : Parcelable