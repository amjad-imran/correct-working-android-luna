package com.oreo.data.model

data class OHSQuestionariesResponseModel(
    val id: String,
    val title: String,
    val description: String,
    var isExpendable: Boolean = false
)