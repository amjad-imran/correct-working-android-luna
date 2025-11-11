package com.oreo.data.model.ai

import com.google.gson.annotations.SerializedName

data class ChatHistoryItem(
    val message: String? = null,
    var date: String? = null,
    var timestamp: Long? = null,
    @SerializedName("thread_id")
    val threadId: String? = null,
    val title: String? = null,
    var isHeader: Boolean = false,
    var headerOther: String? = null
)