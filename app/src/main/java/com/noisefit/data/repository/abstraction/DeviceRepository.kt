package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit_commans.data.model.Feedback
import com.noisefit_commans.data.model.NotificationApp
import com.noisefit_commans.data.model.warranty.MarketPlace
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.response.Watchface2
import com.noisefit_commans.data.model.FeedbackNew
import com.noisefit_commans.data.response.*
import com.noisefit_commans.models.Contact
import kotlinx.coroutines.flow.Flow
import java.io.File

interface DeviceRepository {
    suspend fun getDeviceList(dType: String): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<DeviceListResponse>>>

    suspend fun getDeviceFeature(deviceId: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<DeviceFeatureResponse>>>


    suspend fun getSaveContactInfo(
        selectedContactList: ArrayList<Contact>,
        supportedStpCode: Boolean
    ): Flow<List<Contact>>

    suspend fun getOnlyNumber(
        selectedContactList: ArrayList<Contact>,
        supportedStpCode: Boolean
    ): Flow<HashSet<String>>

    suspend fun getInstalledApps(
        selectedNotificationApp: List<NotificationApp>,
        supportedNotificationApp: List<NotificationApp>
    ): Flow<Pair<ArrayList<NotificationApp>, ArrayList<NotificationApp>>>

    suspend fun enableInstalledAppsNotification(selectedNotificationApp: List<NotificationApp>): Flow<Triple<Boolean, Boolean, List<NotificationApp>>>


    suspend fun getRecentWatchFace(): Flow<Resource<BaseApiResponse<List<Watchface2>?>>>

    suspend fun checkForUpdates(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<UpdateResponse>>>

    suspend fun checkForUpdatesRecent(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<UpdateResponse>>>

    suspend fun getAgpsFileUrl(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.AgpsFileResponse>>>

    suspend fun getVendorAgpsFileUrl(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.AgpsFileResponse>>>

    suspend fun checkWarranty(number: String): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<WarrantyResponse>>>

    suspend fun getMarketPlacesOld(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<String>>>>
    suspend fun checkWatchTokenExist(macAddress: String): Flow<Resource<BaseApiResponse<WatchTokenResponse>>>

    suspend fun removeWatchTokenFromServer(
        macAddress: String
    ): Flow<Resource<BaseApiResponseData<Any>>>


    suspend fun addWarrantyOld(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String?>>>


    suspend fun getMarketPlaces(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<MarketPlace>>>>

    suspend fun getWarrantyWatchList(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<WarrantyWatchesResponse>>>>

    suspend fun addWarranty(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String?>>>

    suspend fun checkWarranty(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<Boolean>>>

    suspend fun getConfig(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<ConfigResponse>>>

    suspend fun submitFeedback(
        feedback: Feedback,
        deviceId: Int? = null
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>>

    suspend fun submitFeedbackNew(
        feedback: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<String>>>

    suspend fun periodicFeedbackFile(
        appLogs: File?,
        ringLogs: File?,
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<Any>>>


    suspend fun submitFeedbackFile(
        feedback: FeedbackNew,
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<String>>>


}