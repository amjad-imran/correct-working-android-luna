package com.oreo.data.model


sealed class ChatGptOverview {

    class SentMessage(
        val message: String,
        val userImage: String?
    ) : ChatGptOverview()

    class ReceivedMessage(
        val message: String
    ) : ChatGptOverview()

    class ThinkingMessage(
    ) : ChatGptOverview()

    class RetryMessage(
        val message: String
    ) : ChatGptOverview()

}