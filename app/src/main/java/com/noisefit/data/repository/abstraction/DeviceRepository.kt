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

    suspend fun checkForUpdates(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<UpdateResponse>>>

    suspend fun checkWatchTokenExist(macAddress: String): Flow<Resource<BaseApiResponse<WatchTokenResponse>>>

    suspend fun removeWatchTokenFromServer(
        macAddress: String
    ): Flow<Resource<BaseApiResponseData<Any>>>

    suspend fun submitFeedbackNew(
        feedback: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<String>>>

    suspend fun periodicFeedbackFile(
        appLogs: File?,
        ringLogs: File?,
        firmwareLogs: File?,
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<Any>>>

    suspend fun reportToDeveloper(
        appLogs: File?,
        ringLogs: File?,
        firmwareLogs: File?,
        title: String,
        description: String?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<Any>>>


    suspend fun submitFeedbackFile(
        feedback: FeedbackNew,
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<String>>>


}