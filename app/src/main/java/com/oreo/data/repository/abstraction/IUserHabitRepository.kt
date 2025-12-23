package com.oreo.data.repository.abstraction

import com.noisefit.data.model.HabitsByDateResponse
import com.noisefit.data.model.HabitsResponse
import com.noisefit.data.model.SyncHabitResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import kotlinx.coroutines.flow.Flow

interface IUserHabitRepository {
    suspend fun getUserHabitsByDate(date: String): Flow<Resource<BaseApiResponse<HabitsByDateResponse>>>
    suspend fun getAllUserHabits(): Flow<Resource<BaseApiResponse<HabitsResponse>>>
    suspend fun syncHabits(ids: List<String>): Flow<Resource<BaseApiResponse<SyncHabitResponse>>>
}