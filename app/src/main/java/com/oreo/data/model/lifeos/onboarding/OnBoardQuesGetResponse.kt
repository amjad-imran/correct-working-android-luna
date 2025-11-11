package com.oreo.data.model.lifeos.onboarding

data class OnBoardQuesGetResponse(
    val answers: List<Answer> ?= null,
    val questions: List<Question> ?= null,
)
