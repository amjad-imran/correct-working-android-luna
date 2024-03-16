package com.oreo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class OtaUpdateModel(
    @SerializedName("firmware_version")
    val firmwareVersion: Int? = null,
    val description: UpdateDescriptionModel? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("firmware_url")
    val firmwareUrl: String? = null,
) : Parcelable
