package com.oreo.data.model.lifeos.onboarding

data class Question(
    val answer: List<AnswerX>,
    val id: Int,
    val text: String,
    val type: String
)

data class AnswerX(
    val addOntext: String,
    val id: Int,
    val text: String
)