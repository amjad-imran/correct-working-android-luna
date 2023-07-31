package com.noisefit_commans.data.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.SportsModeResponse
import kotlinx.parcelize.Parcelize


data class RecentActivities(
    @SerializedName("activities")
    @Expose
    var activities: ArrayList<SportsModeResponse>? = null,
//
    @SerializedName("total")
    @Expose
    val total: Total? = null,
//
    @SerializedName("average")
    @Expose
    val average: Average? = null
)

@Parcelize
data class Average(
    @SerializedName("calories")
    @Expose
    var calories: Int? = null,

    @SerializedName("duration")
    @Expose
    var duration: Int? = null
): Parcelable

@Parcelize
data class Total(
    @SerializedName("calories")
    @Expose
    var calories: Int? = null,

    @SerializedName("duration")
    @Expose
    var duration: Int? = null
): Parcelable