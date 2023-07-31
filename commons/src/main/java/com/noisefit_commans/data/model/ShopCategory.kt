package com.noisefit_commans.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShopCategory(
    val title: String,
    val img: String,
    @SerializedName("collection_id")
    val collectionId: String,
):Parcelable