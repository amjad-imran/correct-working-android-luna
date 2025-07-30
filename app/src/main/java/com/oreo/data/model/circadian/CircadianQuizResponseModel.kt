package com.oreo.data.model.circadian

data class CircadianQuizResponseModel(
    val id: Int?,
    val text: String?,
    val answer: List<CircadianQuizOptionsModel>?,
    var selectedOptionId: Int ?= null
)

data class CircadianQuizOptionsModel(
    val id: Int?,
    val text: String?,
    val score: Int?,
)