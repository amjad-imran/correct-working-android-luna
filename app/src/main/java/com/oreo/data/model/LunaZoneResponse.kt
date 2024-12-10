package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class LunaZoneResponse(
    @SerializedName("summary_available")
    val summaryAvailable: Boolean? = false,
    @SerializedName("SerializedName")
    val workoutPlan: Boolean? = false,
    @SerializedName("nutritional_plan")
    val nutritionalPlan: Boolean? = false,
    @SerializedName("suggested_ques")
    val suggestedQues: List<SuggestedAiQuestions>? = null,

    )

data class SuggestedAiQuestions(
    val ques: String? = null,
    val icon: String? = null,
    val bg_image: String? = null,
)
