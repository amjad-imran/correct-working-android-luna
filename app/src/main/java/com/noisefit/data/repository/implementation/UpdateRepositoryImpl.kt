package com.noisefit.data.repository.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.data.model.AppUpdateModel
import com.noisefit.data.model.OtaUpdateModel
import com.noisefit.data.model.UpdateResponseV2
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UpdateRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.luna.BuildConfig
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.response.BaseApiResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class UpdateRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val ringDataStore: RingDataStore,
    private val localDataStore: DataStoredInterface,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : UpdateRepository {

    override suspend fun checkAppVersionV2(request: JsonObject): Flow<Resource<BaseApiResponse<UpdateResponseV2>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.checkAppVersionV2(
                "${BuildConfig.BASE_URL_NEW}/core/ring/v1/app_version", request
            )
        }
    }

    override suspend fun saveNewAppVersion(appVersion: AppUpdateModel?, currentVersion: Int) {
        if (appVersion == null) {
            localDataStore.cleaNewAppVersion()
            localDataStore.saveAppVersionCheckTimeStamp()
            return
        }

        val gson = gson.toJson(appVersion)
        localDataStore.saveNewAppVersion(gson, currentVersion)
    }

    override suspend fun saveNewOtaVersion(firmwareVersion: OtaUpdateModel?, currentVersion: Int?) {
        if (firmwareVersion == null || currentVersion == null) {
            ringDataStore.cleaNewOtaVersion()
            ringDataStore.saveOtaVersionCheckTimeStamp()
            return
        }

        val gson = gson.toJson(firmwareVersion)
        ringDataStore.saveNewOtaVersion(gson, currentVersion)
    }

    override fun otaRemindLater() {
        ringDataStore.saveOtaRemindDate()
    }

    override fun appRemindLater() {
        localDataStore.saveAppRemindDate()
    }
}