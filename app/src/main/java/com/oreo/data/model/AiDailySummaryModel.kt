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
    val key: String? = null,
    val value: Float? = null,
) : Parcelable

/**
 * activity_score =85
 * date ='2024-12-10'
 * master_avg_hr =70
 * master_avg_hrv =48
 * master_deep =4020
 * master_duration =23280
 * master_mid_time ='03:32:30'
 * master_rem =2970
 * next_period_date ='2024-12-24'
 * readiness_score =79
 * skin_temp_dev =-0.1
 * sleep_need =25200
 * sleep_score =82
 */