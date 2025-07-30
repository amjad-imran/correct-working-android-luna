package com.oreo.data.model


data class CorrectiveActivitiesModel(
    val key: String,
    val bgMainImg: Int,
    val title: String,
    val desc: String,
    val onlyImgWithText: OnlyImgWithText? = null,
    val progressBarLytData: ProgressBarLytData? = null,
    val logStatus: Boolean?,
    val time: String?,
    val timeInSec: Int?
)

data class OnlyImgWithText(
    val img: Int,
    val txt: String? = null
)

data class ProgressBarLytData(
    val totalProgress: Int,
    val currentProgress: Int,
    val img: Int,
    val txt: String
)