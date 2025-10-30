package com.oreo.data.model.ai

import com.google.gson.annotations.SerializedName

data class ChatMessagesResponse(
    @SerializedName("chat_history")
    val chatHistory: List<ChatMessage>? = null
)

data class ChatMessage(
    val message: String? = null,
    val sender: String? = null,
    @SerializedName("attachment_url") val attachmentUrl: String? = null,
    @SerializedName("mime_type") val mimeType: String? = null,
    @SerializedName("document_name") val documentName: String? = null
)
