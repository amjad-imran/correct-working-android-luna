package com.oreo.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import kotlinx.coroutines.flow.Flow

interface OreoDeviceRepository {

    suspend fun savePairingErrorLogs(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>?>>
}