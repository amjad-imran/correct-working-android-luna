package com.noisefit_commans.data.model

import com.google.gson.annotations.SerializedName

data class NplQuizDataModel(
    @SerializedName("elapsed_time")
    val elapsedTime: Long,
    @SerializedName("start_date")
    val startDate: String? = null,
    val hasAnswered: Boolean = false,
    @SerializedName("questions")
    var questions: List<Questions>? = null,

    )

data class Questions(
    @SerializedName("ques_id")
    val quesId: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("correct_ans")
    val correctAns: Long,
    @SerializedName("options")
    val listOptions: List<Options>,
    var userAnswer: Long = 0L //0L not yet answered, -1L no answer, answered Id

)

data class Options(
    @SerializedName("option_id")
    val optionId: Long,
    val title: String
)