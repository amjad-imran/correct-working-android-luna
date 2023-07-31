package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.local.db.CacheResult
import com.noisefit_commans.data.response.HelpAndSupportResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.HelpAndSupportDetailResponse
import com.noisefit_commans.data.response.VersionCheckResponse
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun checkAppVersion(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse>>>

    suspend fun deleteOldTableData(): Flow<CacheResult<Unit?>>

    suspend fun getHelpAndSupportList(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<HelpAndSupportResponse>>>>

    suspend fun getHelpAndSupportByQuestionId(
        id: Int,
        manufacturer: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<HelpAndSupportDetailResponse>>>
}