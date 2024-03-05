package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.model.AppUpdateModel
import com.noisefit.data.model.OtaUpdateModel
import com.noisefit.data.model.UpdateResponseV2
import com.noisefit_commans.data.response.HelpAndSupportResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.HelpAndSupportDetailResponse
import com.noisefit_commans.data.response.VersionCheckResponse
import kotlinx.coroutines.flow.Flow

interface UpdateRepository {

    suspend fun checkAppVersionV2(request: JsonObject): Flow<Resource<BaseApiResponse<UpdateResponseV2>>>

    suspend fun saveNewAppVersion(appVersion: AppUpdateModel?, currentVersion: Int)
    suspend fun saveNewOtaVersion(firmwareVersion: OtaUpdateModel?, currentVersion: Int?)


}