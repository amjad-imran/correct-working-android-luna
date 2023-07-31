package com.noisefit_commans.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoiseHealthCategory(
    @SerializedName("image")
    val image: String = "",
    @SerializedName("sub_categories")
    val subCategories: List<NoiseHealthVideo>,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("icon")
    val icon: String = "",
    @SerializedName("about")
    val about: String = "",
    @SerializedName("category_type")
    val categoryType: String = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("title")
    val title: String = ""
) : Parcelable