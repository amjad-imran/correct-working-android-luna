package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class OHSQuestionariesResponseModel(
    @SerializedName("ques_id")
    var quesId: String = "",
    @SerializedName("ques")
    var question: String = "",
    @SerializedName("ans")
    var answer: String = "",
    var isExpendable: Boolean = false
)