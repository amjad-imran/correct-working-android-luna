package com.oreo.data.model

import androidx.annotation.ColorRes

data class CorrectiveActivitiesModel(
    @ColorRes val bgMainImg: Int,
    val onlyImgWithText: OnlyImgWithText?= null,
    val progressBarLytData: ProgressBarLytData?= null,
    val isOpen: Boolean ?= false,
    val time:String
)

data class OnlyImgWithText(
    @ColorRes val img: Int,
    val txt: String
)

data class ProgressBarLytData(
    val totalProgress: Int,
    val currentProgress: Int,
    @ColorRes val img: Int,
    val txt: String
)