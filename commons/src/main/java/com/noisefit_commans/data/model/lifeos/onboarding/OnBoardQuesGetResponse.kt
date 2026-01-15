package com.noisefit_commans.data.model.lifeos.onboarding

import com.google.gson.annotations.SerializedName

data class OnBoardQuesGetResponse(
    @SerializedName("answer")
    val answers: List<Answer> ?= null,
    val questions: List<Question> ?= null,
)