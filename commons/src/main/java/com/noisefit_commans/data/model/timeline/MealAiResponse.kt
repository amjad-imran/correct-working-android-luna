package com.noisefit_commans.data.model.timeline

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MealAiResponse(
    var id: String?=null,
    var foods: List<MealAiFoods>? = null,
    var macros: List<MealAiMacros>? = null,
    var prompt: String,
    var time: String? = null,
    var date: String?=null,
): Parcelable

@Parcelize
data class MealAiFoods(
    val name: String? = null,
    var calories: Int? = null,
): Parcelable

@Parcelize
data class MealAiMacros(
    val name: String? = null,
    val value: String? = null,
): Parcelable