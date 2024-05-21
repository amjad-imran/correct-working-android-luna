package com.oreo.data.model.femaleh

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class FemaleCycleTrackInfoModel(
    val id: Long? = null,
    val goal: String,
    val diagnosis: List<String>? = null,
    val medicines: List<String>? = null,
    @SerializedName("period_length")
    val periodLength: Int? = null,
    @SerializedName("cycle_length")
    val cycleLength: Int? = null,
    @SerializedName("period_date")
    val periodDate: String? = null
) : Parcelable