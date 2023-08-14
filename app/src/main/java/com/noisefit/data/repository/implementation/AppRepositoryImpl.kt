package com.noisefit.data.repository.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.luna.BuildConfig
import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.HelpAndSupportResponse
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.AppRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class AppRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val lastSyncProvider: LastSyncProvider,
    private val offlineApiStore: IOfflineApiResponseStore,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AppRepository {

    override suspend fun checkAppVersion(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.checkAppVersion(
                "${BuildConfig.BASE_URL_NEW}/core/app_version",
                request
            )
        }
    }

    override suspend fun deleteOldTableData(): Flow<CacheResult<Unit?>> {
        val timeStamp = DateFormats.lastClearDataTimeStamp()
        LOGS.d("deleteOldTableData $timeStamp")
        return safeCacheCall(dispatcher) { //1642962600747

        }


    }

    private fun shouldCallBannerApi(
        serverTime: Long,
        localTime: Long
    ): Boolean {
        if (serverTime == 0L) return true
        if (localTime == 0L) return true

        return localTime < serverTime
    }

    fun getLocalHelpAndSupportData(removeData: Boolean): List<HelpAndSupportResponse>? {
        if (removeData) {
            offlineApiStore.setHelpAndSupportList(null)
            return ArrayList()
        }
        return offlineApiStore.getHelpAndSupportList()
    }

}