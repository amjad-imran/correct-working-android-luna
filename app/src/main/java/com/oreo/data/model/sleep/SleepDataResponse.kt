package com.oreo.data.model.sleep

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class SleepDataResponse(
    val result: List<SleepDay>? = null,
    @SerializedName("register_date")
    val registerDate: Int? = null,
)

@Parcelize
data class SleepLearnMoreDataModel(
    val toolbarTitle: String,
    val title: String? = null,
    val content: String? = null,
    val img: Int? = null,
    val internalImg: Int? = null,
) : Parcelable