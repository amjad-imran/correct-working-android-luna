package com.oreo.data.model

data class ChatGptResponse(
    val reply: String? = null,
    val assistant_id: String? = null,
    val thread_id: String? = null,
    val run_id: String? = null,
    val status: String? = null
)