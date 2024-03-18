package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.oreo.data.model.AppUpdateModel
import com.oreo.data.model.OtaUpdateModel
import com.oreo.data.model.UpdateResponseV2
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import kotlinx.coroutines.flow.Flow

interface UpdateRepository {

    suspend fun checkAppVersionV2(request: JsonObject): Flow<Resource<BaseApiResponse<UpdateResponseV2>>>

    suspend fun saveNewAppVersion(appVersion: AppUpdateModel?, currentVersion: Int)
    suspend fun saveNewOtaVersion(firmwareVersion: OtaUpdateModel?, currentVersion: Int?)

    fun otaRemindLater()
    fun appRemindLater()


}