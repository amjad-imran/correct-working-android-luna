package com.oreo.data.repository.abstraction

import com.google.gson.JsonObject
import com.oreo.data.model.timeline.habits.HabitsByDateResponse
import com.oreo.data.model.timeline.habits.HabitsResponse
import com.noisefit.data.model.SyncHabitResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import kotlinx.coroutines.flow.Flow

interface IUserHabitRepository {
    suspend fun getUserHabitsByDate(date: String): Flow<Resource<BaseApiResponse<HabitsByDateResponse>>>
    suspend fun getAllUserHabits(): Flow<Resource<BaseApiResponse<HabitsResponse>>>
    suspend fun syncHabits(ids: List<String>): Flow<Resource<BaseApiResponse<SyncHabitResponse>>>

    suspend fun submitUserHabits(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun cancelUserHabitsByIdAndDate(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>>
}