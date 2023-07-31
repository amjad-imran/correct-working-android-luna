package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.model.*
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.model.NplQuizDataModel
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.CollectCoinResponse
import com.noisefit_commans.data.response.NplDashResponse
import kotlinx.coroutines.flow.Flow

interface NPLRepository {
    suspend fun getNplDashScoreCard(): Flow<Resource<BaseApiResponse<ScoreCardData>>>
    suspend fun getNplProfile(): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun getPredictionHistory(): Flow<Resource<BaseApiResponse<List<PredictionHistoryData>>>>
    suspend fun collectNplReward(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<CollectCoinResponse>>>

    suspend fun getMatchesData(): Flow<Resource<BaseApiResponse<NplDashResponse>>>
    suspend fun predictWinner(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun getNplWins(): Flow<Resource<BaseApiResponse<WinListData>>>
    suspend fun getNplQuizData(): Flow<Resource<BaseApiResponse<NplQuizDataModel>>>
    suspend fun submitQuizAnswer(jsonObject: JsonObject):Flow<Resource<BaseApiResponse<SubmitAnswerDataModel>>>

    suspend fun collectNplQuizReward(jsonObject: JsonObject):Flow<Resource<BaseApiResponse<Any>>>
}