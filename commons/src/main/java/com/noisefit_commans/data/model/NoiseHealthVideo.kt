package com.noisefit_commans.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoiseHealthVideo(
    @SerializedName("duration")
    val duration: String = "",
    @SerializedName("image")
    val image: String = "",
    @SerializedName("category_id")
    val categoryId: Int = 0,
    @SerializedName("sub_category_type")
    val subCategoryType: String = "",
    @SerializedName("media_type")
    val mediaType: String = "",
    @SerializedName("user_playtime")
    var userPlaytime: Int = 0,
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("source")
    val source: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("views")
    val views: Int = 0,
    @SerializedName("content")
    val content: String = ""
) : Parcelable