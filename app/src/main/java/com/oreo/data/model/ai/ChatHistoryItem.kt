package com.oreo.data.model.ai

import com.google.gson.annotations.SerializedName

data class ChatHistoryItem(
    val message: String? = null,
    val date: String? = null,
    @SerializedName("thread_id")
    val threadId: String,
    val title: String,
    var isHeader: Boolean = false
)