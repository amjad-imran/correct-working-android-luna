package com.noisefit.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppUpdateModel(
    val title: String? = null,
    val message: String? = null,
    @SerializedName("long_message")
    val longMessage: String? = null,
    @SerializedName("build_version")
    val buildVersion: Int? = null,
    @SerializedName("app_version")
    val appVersion: String? = null,
    @SerializedName("back_url")
    val backUrl: String? = null,
) : Parcelable
