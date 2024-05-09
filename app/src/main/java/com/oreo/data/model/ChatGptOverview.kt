package com.oreo.data.model


sealed class ChatGptOverview {

    class SentMessage(
        val message: String
    ) : ChatGptOverview()

    class ReceivedMessage(
        val thinking: Boolean = false,
        val message: String
    ) : ChatGptOverview()

}