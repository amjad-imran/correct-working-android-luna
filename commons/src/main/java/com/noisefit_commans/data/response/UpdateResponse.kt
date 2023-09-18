package com.noisefit_commans.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateResponse(
    @SerializedName("description_english")
    val descriptionEnglish: String? = null,
    @SerializedName("forceUpdate")
    var forceUpdate: Boolean = false,
    @SerializedName("softUpdate")
    val softUpdate: Boolean = false,
    @SerializedName("version")
    val version: Int = 0,
    @SerializedName("url")
    val url: String? = null,
    val version_name: String? = null,
    @SerializedName("hr_url")
    val hrUrl: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null
) : Parcelable