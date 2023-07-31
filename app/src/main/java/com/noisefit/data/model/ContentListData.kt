package com.noisefit.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ContentListData(
    @SerializedName("last_watched")
    @Expose
    val lastWatched: List<LastWatchedList>? = null,
    @SerializedName("video_categories")
    @Expose
    val videoCategories: List<VideoCategoriesList>? = null,
)

data class LastWatchedList(
    @SerializedName("video_id")
    @Expose
    val videoId: Int? = null,
    @SerializedName("thumbnail_url")
    @Expose
    val thumbnailUrl: String? = null,
    @SerializedName("video_url")
    @Expose
    val videoUrl: String? = null,
    @SerializedName("progress")
    @Expose
    val progress: Int? = null,
    @SerializedName("duration")
    @Expose
    val duration: Long? = null,
    @SerializedName("tags")
    @Expose
    val tags: String? = null,
    @SerializedName("title")
    @Expose
    val title: String? = null
)

data class VideoCategoriesList(
    @SerializedName("category_id")
    @Expose
    val categoryId: Int? = null,
    @SerializedName("title")
    @Expose
    val title: String? = null,
    @SerializedName("subCategories")
    @Expose
    val subCategories: List<SubCategoriesList>? = null,

    @SerializedName("videos")
    @Expose
    val videos: List<VideosList>? = null,

    ) {
    var defaultSelectedPosition = 0
}

data class SubCategoriesList(
    @SerializedName("title")
    @Expose
    var title: String? = null,
    @SerializedName("subcategory_id")
    @Expose
    var subcategoryId: Int? = null,

    var itemSelected: Boolean = false

)

data class VideosList(
    @SerializedName("id")
    @Expose
    val id: Int? = null,
    @SerializedName("category_id")
    @Expose
    val categoryId: Int? = null,
    @SerializedName("subcategory_id")
    @Expose
    val subcategoryId: Int? = null,
    @SerializedName("title")
    @Expose
    val title: String? = null,
    @SerializedName("views")
    @Expose
    val views: Int? = null,
    @SerializedName("video_url")
    @Expose
    val videoUrl: String? = null,
    @SerializedName("thumbnail_url")
    @Expose
    val thumbnailUrl: String? = null,
    @SerializedName("tags")
    @Expose
    val tags: String? = null,
    @SerializedName("duration")
    @Expose
    val duration: Long? = null,
    @SerializedName("equipments")
    @Expose
    val equipments: String? = null,
    @SerializedName("detail")
    @Expose
    val detail: String? = null,

    )