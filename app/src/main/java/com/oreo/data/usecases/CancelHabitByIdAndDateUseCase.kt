package com.oreo.data.usecases

import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.repository.abstraction.IUserHabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CancelHabitByIdAndDateUseCase @Inject constructor(
    private val repository: IUserHabitRepository
) {
    suspend operator fun invoke(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return repository.cancelUserHabitsByIdAndDate(req)
    }
}