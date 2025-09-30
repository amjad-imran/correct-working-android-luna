package com.noisefit.data.model.timeline

data class MealAiResponse(
    val foods: List<MealAiFoods>? = null,
    val macros: List<MealAiMacros>? = null,
    val prompt: String,
    val time: String? = null
)

data class MealAiFoods(
    val name: String? = null,
    var calories: Int? = null,
)

data class MealAiMacros(
    val name: String? = null,
    val value: String? = null,
)