package com.noisefit_commans.data.response

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HelpAndSupportResponse(
    @SerializedName("title")
    @Expose
    val title: String? = null,
    @SerializedName("id")
    @Expose
    val id: Int? = null,
    @SerializedName("type")
    @Expose
    val type: String? = null,
    @SerializedName("subtitle")
    @Expose
    val subtitle: String? = null,
    @SerializedName("icon_url")
    @Expose
    val icon: String? = null,
    @SerializedName("questions")
    @Expose
    val questionsList: List<HelpAndSupportQuestion>? = null
) : Parcelable

@Parcelize
data class HelpAndSupportQuestion(
    @SerializedName("ques_id")
    @Expose
    val id: Int? = null,
    @SerializedName("question")
    @Expose
    val question: String? = null,
) : Parcelable