package com.noisefit.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppUpdateModel(
    @SerializedName("app_version")
    val appVersion: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    val description: UpdateDescriptionModel? = null
) : Parcelable

@Parcelize
data class UpdateDescriptionModel(
    val header: String? = null,
    @SerializedName("short_description")
    val shortDescription: String? = null,
    @SerializedName("long_description")
    val longDescription: String? = null
) : Parcelable
