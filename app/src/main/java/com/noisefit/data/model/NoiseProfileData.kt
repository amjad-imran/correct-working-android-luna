package com.noisefit.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NoiseProfileData(
    @SerializedName("first_name")
    @Expose
    val firstName: String? = null,
    @SerializedName("image_url")
    @Expose
    val imageUrl: String? = null,
    @SerializedName("community")
    @Expose
    val community: Int = 0,
    @SerializedName("post_count")
    @Expose
    val postCount: Int = 0,
    @SerializedName("introduction")
    @Expose
    val introduction: String? = null
)
