package com.noisefit.data.repository.implementation

import com.google.gson.JsonObject
import com.noisefit.BuildConfig
import com.noisefit.data.model.*
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.NPLRepository
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit_commans.data.model.NplQuizDataModel
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.CollectCoinResponse
import com.noisefit_commans.data.response.NplDashResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class NPLRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val userActivityRepository: UserActivityRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : NPLRepository {
    override suspend fun getNplDashScoreCard(): Flow<Resource<BaseApiResponse<ScoreCardData>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getNplDashScoreCard("${BuildConfig.BASE_URL_NEW}/npl/v2/profile/scorecard")
        }
    }

    override suspend fun getMatchesData(): Flow<Resource<BaseApiResponse<NplDashResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getMatchesData("${BuildConfig.BASE_URL_NEW}/npl/profile/matches")
        }
    }

    override suspend fun predictWinner(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.predictWinner(
                "${BuildConfig.BASE_URL_NEW}/npl/user/prediction",
                jsonObject
            )
        }
    }

    override suspend fun getNplProfile(): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            //todo add end point url here
            remoteDataSource.getNplProfile(BuildConfig.BASE_URL_NEW)
        }
    }

    override suspend fun getPredictionHistory(): Flow<Resource<BaseApiResponse<List<PredictionHistoryData>>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getPredictionHistory("${BuildConfig.BASE_URL_NEW}/npl/prediction/history")
        }
    }

    override suspend fun collectNplReward(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<CollectCoinResponse>>> {
        return safeApiCallFlow(dispatcher) {
            userActivityRepository.removeLocalStreakData()
            remoteDataSource.collectReward(
                "${BuildConfig.BASE_URL_NEW}/npl/user/collect",
                jsonObject
            )
        }
    }

    override suspend fun getNplWins(): Flow<Resource<BaseApiResponse<WinListData>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getNplWins("${BuildConfig.BASE_URL_NEW}/npl/previous/wins")
        }
    }

    override suspend fun getNplQuizData(): Flow<Resource<BaseApiResponse<NplQuizDataModel>>> {
        return safeApiCallFlow(dispatcher){
            remoteDataSource.getNplQuizData("${BuildConfig.BASE_URL_NEW}/npl/v2/quiz/questions")
        }

    }

    override suspend fun submitQuizAnswer(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<SubmitAnswerDataModel>>> {
        return safeApiCallFlow(dispatcher){
            remoteDataSource.submitQuizAnswer("${BuildConfig.BASE_URL_NEW}/npl/v2/quiz/submit/answers",jsonObject)
        }

    }

    override suspend fun collectNplQuizReward(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher){
            remoteDataSource.collectQuizWinReward("${BuildConfig.BASE_URL_NEW}/rewards/npl_quiz/collect",jsonObject)
        }
    }
}