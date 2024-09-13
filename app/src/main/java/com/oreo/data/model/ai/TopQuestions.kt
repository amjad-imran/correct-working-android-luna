package com.oreo.data.model.ai


data class TopQuestionsResponse(
    val questions: List<TopQuestions>? = null,
    val hasHistory: Boolean = false
)

data class TopQuestions(
    val question: String? = null
)
