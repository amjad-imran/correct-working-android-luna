package com.oreo.data.repository.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.safeApiCallFlow
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.model.ChatGptResponse
import com.oreo.data.model.ai.ChatHistoryItem
import com.oreo.data.model.ai.ChatMessagesResponse
import com.oreo.data.model.ai.ThreadIdResponse
import com.oreo.data.model.ai.TopQuestionsResponse
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import com.oreo.ui.chatGpt.AITopics
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow


class OreoDeviceRepositoryImpl(
    private val localDatSource: DataStoredInterface,
    private val remoteDataSource: NetworkService,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : OreoDeviceRepository {

    override suspend fun savePairingErrorLogs(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.OREO_BASE_URL}/logging/create/pairing_logs"
            remoteDataSource.savePairingErrorLogs(url, jsonObject)
        }
    }

    override suspend fun askQuestionToChatGpt(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<ChatGptResponse>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/ai-bridge/chat"
            remoteDataSource.askQuestionToChatGpt(url, jsonObject)

        }
    }

    override suspend fun pollForAnswer(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<ChatGptResponse>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/ai-bridge/message/polling"
            remoteDataSource.pollForAnswer(url, jsonObject)

        }
    }

    override suspend fun getChatHistory(): Flow<Resource<BaseApiResponse<List<ChatHistoryItem>?>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/ai-bridge/chat-history"
            remoteDataSource.getChatHistory(url)

        }
    }

    override suspend fun getChatHistoryByDate(date: String?): Flow<Resource<BaseApiResponse<List<ChatHistoryItem>?>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/ai-bridge/date-history"
            remoteDataSource.getChatHistoryByDate(url, date)
        }
    }

    override suspend fun deleteChatHistory(threadId: String): Flow<Resource<BaseApiResponse<Any?>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/ai-bridge/delete"
            remoteDataSource.deleteChatHistory(url, threadId)
        }
    }

    override suspend fun generateThreadId(): Flow<Resource<BaseApiResponse<ThreadIdResponse?>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/ai-bridge/new-chat"
            remoteDataSource.generateThreadId(url)
        }
    }

    override suspend fun loadMessagesByThreadId(threadId: String): Flow<Resource<BaseApiResponse<ChatMessagesResponse>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/ai-bridge/chat?thread_id=$threadId"
            remoteDataSource.loadMessagesByThreadId(url)
        }
    }

    override suspend fun generateThreadTitle(
        ques: String,
        threadId: String
    ): Flow<Resource<BaseApiResponse<String>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/ai-bridge/generateTitle"
            remoteDataSource.generateThreadTitle(url, ques, threadId)
        }
    }

    override suspend fun stopResponseGeneration(threadId: String): Flow<Resource<BaseApiResponse<Any>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/ai-bridge/stopStream?thread_id=$threadId"
            remoteDataSource.stopResponseGeneration(url)
        }
    }

    override suspend fun getAiTopQuestions(aiTopic: AITopics): Flow<Resource<BaseApiResponse<TopQuestionsResponse>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/ai-bridge/suggested-questions?type=${aiTopic.name.lowercase()}"
            remoteDataSource.getAiTopQuestions(url)
        }
    }
}