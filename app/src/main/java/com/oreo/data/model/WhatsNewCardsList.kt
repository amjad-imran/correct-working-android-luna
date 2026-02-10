package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class WhatsNewCardsList (
    val android: Map<String, List<BannerItem>>?,
)

data class BannerItem(
    val id: String,
    @SerializedName("image_url")
    val imageUrl: Map<String, String>,
    @SerializedName("deeplink_action")
    val deeplinkAction: String,
    @SerializedName("blog_details")
    val blogDetails: BlogDetails?,
    @SerializedName("tap_event_detail")
    val tapEventDetail: String,
    @SerializedName("show_cross_button")
    val showCrossButton: Boolean
)

data class BlogDetails(
    @SerializedName("blog_title")
    val blogTitle: Map<String, String>,
    @SerializedName("blog_description_md")
    val blogDescriptionMd: Map<String, String>
)