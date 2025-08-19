package com.oreo.data.model


data class CorrectiveActivitiesModel(
    val key: String,
    val bgMainImg: Int,
    val title: String,
    val desc: String,
    var onlyImgWithText: OnlyImgWithText? = null,
    var progressBarLytData: ProgressBarLytData? = null,
    var onlyOnlyImgLytData: Int? = null,
    var showFooter: Boolean? = false,
    var logStatus: Boolean?=null,
    var time: String?=null,
    var timeInSec: Int?=null,
)

data class OnlyImgWithText(
    val img: Int,
    var txt: String? = null
)

data class ProgressBarLytData(
    val totalProgress: Int?,
    val currentProgress: Int,
    val img: Int,
    val txt: String
)