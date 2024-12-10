package com.oreo.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.model.AiDailySummaryModel
import com.oreo.data.model.ChatGptResponse
import com.oreo.data.model.LunaZoneResponse
import com.oreo.data.model.ai.ChatHistoryItem
import com.oreo.data.model.ai.ChatMessagesResponse
import com.oreo.data.model.ai.ThreadIdResponse
import com.oreo.data.model.ai.TopQuestionsResponse
import com.oreo.ui.chatGpt.AITopics
import kotlinx.coroutines.flow.Flow

interface OreoDeviceRepository {

    suspend fun savePairingErrorLogs(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>?>>

    suspend fun askQuestionToChatGpt(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<ChatGptResponse>?>>

    suspend fun pollForAnswer(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<ChatGptResponse>?>>

    suspend fun getChatHistory(): Flow<Resource<BaseApiResponse<List<ChatHistoryItem>?>?>>

    suspend fun getChatHistoryByDate(date: String?): Flow<Resource<BaseApiResponse<List<ChatHistoryItem>?>?>>

    suspend fun deleteChatHistory(threadId: String): Flow<Resource<BaseApiResponse<Any?>?>>

    suspend fun generateThreadId(): Flow<Resource<BaseApiResponse<ThreadIdResponse?>?>>

    suspend fun loadMessagesByThreadId(threadId: String): Flow<Resource<BaseApiResponse<ChatMessagesResponse>?>>

    suspend fun generateThreadTitle(
        ques: String,
        threadId: String
    ): Flow<Resource<BaseApiResponse<String>?>>

    suspend fun stopResponseGeneration(threadId: String): Flow<Resource<BaseApiResponse<Any>?>>

    suspend fun getAiTopQuestions(aiTopic: AITopics): Flow<Resource<BaseApiResponse<TopQuestionsResponse>?>>

    suspend fun getAiWorkoutPlans(): Flow<Resource<BaseApiResponse<Any>?>>

    suspend fun getAiMealPlans(): Flow<Resource<BaseApiResponse<Any>?>>

    suspend fun getDailySummaryData(): Flow<Resource<BaseApiResponse<List<AiDailySummaryModel>>?>>

    suspend fun getLunaZoneData(): Flow<Resource<BaseApiResponse<LunaZoneResponse>?>>

    suspend fun saveWorkoutPlan(): Flow<Resource<BaseApiResponse<Any>?>>

    suspend fun saveMealPlan(): Flow<Resource<BaseApiResponse<Any>?>>
}