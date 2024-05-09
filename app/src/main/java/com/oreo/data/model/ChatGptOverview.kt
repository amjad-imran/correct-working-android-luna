package com.oreo.data.model


sealed class ChatGptOverview {

    class SentMessage(
        val message: String,
        val userImage:String?
    ) : ChatGptOverview()

    class ReceivedMessage(
        val thinking: Boolean = false,
        val message: String
    ) : ChatGptOverview()

}