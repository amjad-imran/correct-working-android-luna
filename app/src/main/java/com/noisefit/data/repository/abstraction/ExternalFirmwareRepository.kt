package com.noisefit.data.repository.abstraction

import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.MessageResponse
import kotlinx.coroutines.flow.Flow

interface ExternalFirmwareRepository {
    suspend fun getNavFirmware(
        productCode : String
    ): Flow<Resource<MessageResponse?>>

    suspend fun getAccessToken(): Flow<Resource<MessageResponse?>>

}