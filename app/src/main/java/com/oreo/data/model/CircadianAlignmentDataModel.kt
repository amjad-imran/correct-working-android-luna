package com.oreo.data.model

import androidx.annotation.DrawableRes

data class CorrectiveActivitiesModel(
    val key: String,
    @DrawableRes val bgMainImg: Int,
    val title: String,
    val desc: String,
    val onlyImgWithText: OnlyImgWithText?= null,
    val progressBarLytData: ProgressBarLytData?= null,
    val logStatus: Boolean?,
    val time:String?,
    val timeInSec: Int?
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