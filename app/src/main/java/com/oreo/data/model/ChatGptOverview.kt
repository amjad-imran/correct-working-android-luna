package com.oreo.data.model

import com.noisefit.data.model.AiMeals
import com.noisefit.data.model.AiWorkout
import java.util.UUID


sealed class ChatGptOverview(var id: UUID = UUID.randomUUID()) {

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
        val workout: AiWorkout
    ) : ChatGptOverview()

    class HeaderMeal(
        val meal: AiMeals
    ) : ChatGptOverview()

    class RetryMessage(
        val message: String
    ) : ChatGptOverview()

}