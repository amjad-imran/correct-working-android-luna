package com.noisefit_commans.data.response

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HelpAndSupportDetailResponse(
    @SerializedName("question")
    @Expose
    val question: String? = null,
    @SerializedName("short_ans")
    @Expose
    val shortAnswer: String? = null,
    @SerializedName("video_url")
    @Expose
    val videoUrl: String? = null,
    @SerializedName("thumbnail_url")
    @Expose
    val thumbnailUrl: String? = null,
    val thumbnail: String? = null,
    @SerializedName("content")
    @Expose
    val content: List<HelpAndSupportContent>? = null,
    @SerializedName("videos")
    @Expose
    val videoList: List<VideoData>? = null,
) : Parcelable

@Parcelize
data class HelpAndSupportContent(
    @SerializedName("title")
    @Expose
    val title: String? = null,
    @SerializedName("text")
    @Expose
    val text: String? = null,
    @SerializedName("media_type")
    @Expose
    val mediaType: String? = null,
    @SerializedName("action_id")
    @Expose
    val actionId: Int? = null,
    @SerializedName("media_url")
    @Expose
    val mediaUrl: ArrayList<String>? = null,
) : Parcelable

@Parcelize
data class VideoData(
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("thumbnail")
    val thumbnail: String? = null,
    @SerializedName("language")
    val language: String? = null
) : Parcelable
