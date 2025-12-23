package com.oreo.data.usecases

import com.noisefit.data.model.SyncHabitResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.repository.abstraction.IUserHabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncUserHabitsUseCase @Inject constructor(
    private val repository: IUserHabitRepository
) {
    suspend operator fun invoke(ids: List<String>): Flow<Resource<BaseApiResponse<SyncHabitResponse>>> {
        return repository.syncHabits(ids)
    }
}