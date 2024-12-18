package com.oreo.data.model

import com.noisefit.data.model.AiExerciseList


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

    class HeaderWorkout(
        val workout: AiExerciseList
    ) : ChatGptOverview()

    class HeaderMeal(
        val meal: String
    ) : ChatGptOverview()

    class RetryMessage(
        val message: String
    ) : ChatGptOverview()

}