package com.oreo.data.repository.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.data.model.AiMealResponse
import com.noisefit.data.model.AiWorkoutResponse
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.safeApiCallFlow
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.chatGPT.voice.persona.ItemPersonaVoiceResponse
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.model.AiCreds
import com.oreo.data.model.AiDailySummaryModel
import com.oreo.data.model.ChatGptResponse
import com.oreo.data.model.LunaZoneResponse
import com.oreo.data.model.ai.ChatHistoryItem
import com.oreo.data.model.ai.ChatMessagesResponse
import com.oreo.data.model.ai.ThreadIdResponse
import com.oreo.data.model.ai.TopQuestionsResponse
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.PlanType
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
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/chat"
            remoteDataSource.askQuestionToChatGpt(url, jsonObject)

        }
    }

    override suspend fun pollForAnswer(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<ChatGptResponse>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/message/polling"
            remoteDataSource.pollForAnswer(url, jsonObject)

        }
    }

    override suspend fun getChatHistory(): Flow<Resource<BaseApiResponse<List<ChatHistoryItem>?>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/chat-history"
            remoteDataSource.getChatHistory(url)

        }
    }

    override suspend fun getChatHistoryByDate(date: String?): Flow<Resource<BaseApiResponse<List<ChatHistoryItem>?>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/date-history"
            remoteDataSource.getChatHistoryByDate(url, date)
        }
    }

    override suspend fun deleteChatHistory(threadId: String): Flow<Resource<BaseApiResponse<Any?>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/delete"
            remoteDataSource.deleteChatHistory(url, threadId)
        }
    }

    override suspend fun generateThreadId(): Flow<Resource<BaseApiResponse<ThreadIdResponse?>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/new-chat"
            remoteDataSource.generateThreadId(url)
        }
    }

    override suspend fun loadMessagesByThreadId(threadId: String): Flow<Resource<BaseApiResponse<ChatMessagesResponse>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/chat?thread_id=$threadId"
            remoteDataSource.loadMessagesByThreadId(url)
        }
    }

    override suspend fun generateThreadTitle(
        ques: String,
        threadId: String
    ): Flow<Resource<BaseApiResponse<String>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/generateTitle"
            remoteDataSource.generateThreadTitle(url, ques, threadId)
        }
    }

    override suspend fun stopResponseGeneration(
        threadId: String?,
        planType: PlanType
    ): Flow<Resource<BaseApiResponse<Any>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = if (planType == PlanType.NONE) {
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/stopStream?thread_id=$threadId"
            } else {
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/stopStream?type=${planType.name.lowercase()}"
            }
            remoteDataSource.stopResponseGeneration(url)
        }
    }

    override suspend fun getAiTopQuestions(aiTopic: AITopics): Flow<Resource<BaseApiResponse<TopQuestionsResponse>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/suggested-questions?type=${aiTopic.name.lowercase()}"
            remoteDataSource.getAiTopQuestions(url)
        }
    }

    override suspend fun getAiWorkoutPlans(): Flow<Resource<BaseApiResponse<List<AiWorkoutResponse>>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/activity/v2/workout-plan"
            remoteDataSource.getAiWorkoutPlans(url)
        }
    }

    override suspend fun getAiRelaxedWorkoutPlans(): Flow<Resource<BaseApiResponse<AiWorkoutResponse>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/workout/women"
            remoteDataSource.getAiRelaxedWorkoutPlans(url)
        }
    }

    override suspend fun getAiMealPlans(): Flow<Resource<BaseApiResponse<List<AiMealResponse>>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/activity/v2/diet-plan"
            remoteDataSource.getAiMealPlans(url)
        }
    }

    override suspend fun getAiComfortMealPlans(): Flow<Resource<BaseApiResponse<AiMealResponse>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/diet/women"
            remoteDataSource.getAiComfortMealPlans(url)
        }
    }

    override suspend fun getAiBoosterMealPlans(): Flow<Resource<BaseApiResponse<AiMealResponse>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/diet/booster"
            remoteDataSource.getAiBoosterMealPlans(url)
        }
    }

    override suspend fun getDailySummaryData(): Flow<Resource<BaseApiResponse<List<AiDailySummaryModel>>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/digest"
            remoteDataSource.getDailySummaryData(url)
        }
    }

    override suspend fun getLunaZoneData(): Flow<Resource<BaseApiResponse<LunaZoneResponse>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/dashboard"
            remoteDataSource.getLunaZoneData(url)
        }
    }

    override suspend fun saveWorkoutPlan(): Flow<Resource<BaseApiResponse<Any>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/activity/v2/workout-plan"
            remoteDataSource.saveWorkoutPlan(url)
        }
    }

    override suspend fun saveMealPlan(): Flow<Resource<BaseApiResponse<Any>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/activity/v2/diet-plan"
            remoteDataSource.saveWorkoutPlan(url)
        }
    }

    override suspend fun getCredentials(): Flow<Resource<BaseApiResponse<AiCreds>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/ai-credentials"
            remoteDataSource.getCredentials(url)
        }
    }

    override suspend fun markAiMessageState(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>?>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v1/chat-review"
            remoteDataSource.markAiMessageState(url,jsonObject)
        }
    }

    override suspend fun getPersonaVoiceData(): Flow<Resource<BaseApiResponse<List<ItemPersonaVoiceResponse>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${com.noisefit.luna.BuildConfig.BASE_URL_NEW}/luna/ai/v2/persona"
            remoteDataSource.getPersonaVoiceData(url)
        }
    }
}