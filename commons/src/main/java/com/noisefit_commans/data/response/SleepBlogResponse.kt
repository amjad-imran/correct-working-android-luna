package com.noisefit_commans.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SleepBlogCategories (
    @SerializedName("id") val id : Int,
    @SerializedName("title") val title : String,
    @SerializedName("sub_title") val sub_title : String,
    @SerializedName("image") val image : String,
    @SerializedName("description") val description : String,
    @SerializedName("sleep_sub_categories") val sleepSubCategories : ArrayList<SleepBlogSubCategories>
): Parcelable

@Parcelize
data class SleepBlogSubCategories (
    @SerializedName("id") val id : Int,
    @SerializedName("title") val title : String,
    @SerializedName("image") val image : String,
    @SerializedName("description") val description : String,
    @SerializedName("sleep_category_id") val sleepCategoryId : Int
): Parcelable


@Parcelize
data class HowStressResponse (
    @SerializedName("id") val id : Int,
    @SerializedName("title") val title : String,
    @SerializedName("image") val image : String,
): Parcelable