package com.oreo.data.usecases

import com.oreo.data.model.timeline.habits.HabitsByDateResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.repository.abstraction.IUserHabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHabitsByDateUseCase @Inject constructor(
    private val repository: IUserHabitRepository
) {
    suspend operator fun invoke(date: String): Flow<Resource<BaseApiResponse<HabitsByDateResponse>>> {
        return repository.getUserHabitsByDate(date)
    }
}