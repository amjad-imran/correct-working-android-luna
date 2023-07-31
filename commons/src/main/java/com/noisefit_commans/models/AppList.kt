package com.noisefit_commans.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Widget(
    @SerializedName("functionId") var functionId: Int,
    @SerializedName("name") var name: String,
    @SerializedName("haveHide") var haveHide: Boolean,
    @SerializedName("isEnable") var isEnable: Boolean,
    @SerializedName("order") var order: Int,
    @SerializedName("sortable") var sortable: Boolean
) : Parcelable