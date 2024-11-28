package com.noisefit.ui.settings.feedbacknew

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FeedbackQuestionaries(
    @SerializedName("question_en") @Expose var question_en: String? = null,
    @SerializedName("question_de") @Expose var question_de: String? = null,
    @SerializedName("question_es") @Expose var question_es: String? = null,
    @SerializedName("question_fr") @Expose var question_fr: String? = null,
    @SerializedName("question_it") @Expose var question_it: String? = null,
    @SerializedName("question_nl") @Expose var question_nl: String? = null,
    @SerializedName("question_pt") @Expose var question_pt: String? = null,
    @SerializedName("question_ru") @Expose var question_ru: String? = null,
    @SerializedName("question_th") @Expose var question_th: String? = null,
    @SerializedName("question_zh") @Expose var question_zh: String? = null,

    @SerializedName("problem_array") @Expose var problem_array: List<String>? = null
)
