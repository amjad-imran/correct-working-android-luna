package com.noisefit.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class AiMealResponse(
    val day_name: String? = null,//day_1, day_2
    val meals: List<AiMeals>? = null
)

@Parcelize
data class AiMeals(
    val meal_type: String? = null,
    val img: String? = null,
    val meal: List<AiMeal>? = null,
) : Parcelable

@Parcelize
data class AiMeal(
    val portion: String? = null,
    val meal_name: String? = null,
    val description: String? = null,
) : Parcelable