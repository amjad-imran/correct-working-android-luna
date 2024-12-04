package com.oreo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AiDailySummaryModel(
    val title: String? = null,
    @SerializedName("sub_title")
    val subTitle: String? = null,
    @SerializedName("bg_image")
    val bgImage:String?=null,
    val metrics: List<DataMetrics>? = null
) : Parcelable

@Parcelize
data class DataMetrics(
    val name: String? = null,
    val value: Float? = null,
    val unit: String? = null,
) : Parcelable
