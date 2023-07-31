package com.noisefit.ui.settings.feedbacknew

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FeedbackQuestionaries(
    @SerializedName("question") @Expose var question: String? = null,
    @SerializedName("problem_array") @Expose var problem_array: List<String>? = null
)
