package com.oreo.data.model

import androidx.annotation.DrawableRes

data class CorrectiveActivitiesModel(
    @DrawableRes val bgMainImg: Int,
    val title: String,
    val desc: String,
    val onlyImgWithText: OnlyImgWithText?= null,
    val progressBarLytData: ProgressBarLytData?= null,
    val isOpen: Boolean ?= false,
    val time:String
)

data class OnlyImgWithText(
    @DrawableRes val img: Int,
    val txt: String ?= null
)

data class ProgressBarLytData(
    val totalProgress: Int,
    val currentProgress: Int,
    @DrawableRes val img: Int,
    val txt: String
)

data class QuizQuestionCircadianDataModel(
    val text: String,
    val options: List<String>,
    var selectedOption: String? = null
)