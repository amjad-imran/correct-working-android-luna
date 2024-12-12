package com.oreo.ui.chatGpt.audio

data class ChatCompletionResponse(
    val choices: List<Choice>? = null
)

data class Choice(
    val index: Int,
    val delta: Delta? = null
)

data class Delta(
    val role: String? = null,
    val audio: Audio? = null
)

data class Audio(
    val id: String? = null,
    val data: String? = null,
    val transcript: String? = null
)
