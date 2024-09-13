package com.oreo.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.model.ChatGptResponse
import com.oreo.data.model.ai.ChatHistoryItem
import com.oreo.data.model.ai.ChatMessagesResponse
import com.oreo.data.model.ai.ThreadIdResponse
import com.oreo.data.model.ai.TopQuestionsResponse
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

    suspend fun getAiTopQuestions(): Flow<Resource<BaseApiResponse<TopQuestionsResponse>?>>

}