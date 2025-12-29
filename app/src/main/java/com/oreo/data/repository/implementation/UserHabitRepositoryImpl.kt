package com.oreo.data.repository.implementation

import com.google.gson.JsonObject
import com.oreo.data.model.timeline.habits.HabitsByDateResponse
import com.oreo.data.model.timeline.habits.HabitsResponse
import com.noisefit.data.model.SyncHabitResponse
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.safeApiCallFlow
import com.noisefit.luna.BuildConfig
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.repository.abstraction.IUserHabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class UserHabitRepositoryImpl(val networkService: NetworkService) : IUserHabitRepository {
    override suspend fun getUserHabitsByDate(date: String): Flow<Resource<BaseApiResponse<HabitsByDateResponse>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            networkService.getUserHabitsByDate("${BuildConfig.OREO_BASE_URL}/protean/v3/time-tracker/user-habits", date)
        }
    }

    override suspend fun getAllUserHabits(): Flow<Resource<BaseApiResponse<HabitsResponse>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            networkService.getAllUserHabits("${BuildConfig.OREO_BASE_URL}/protean/v3/time-tracker/habits")
        }
    }

    override suspend fun syncHabits(ids: List<String>): Flow<Resource<BaseApiResponse<SyncHabitResponse>>> {
        TODO("Not yet implemented")
    }

    override suspend fun submitUserHabits(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            networkService.submitUserHabits(
                "${BuildConfig.OREO_BASE_URL}/protean/v3/time-tracker/user-habits",
                req
            )
        }
    }

    override suspend fun cancelUserHabitsByIdAndDate(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            networkService.submitUserHabits(
                "${BuildConfig.OREO_BASE_URL}/protean/v3/time-tracker/cancel-user-habit",
                req
            )
        }
    }

}