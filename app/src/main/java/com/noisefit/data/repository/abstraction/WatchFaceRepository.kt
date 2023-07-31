package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.OfflineResult
import com.noisefit.data.model.diy.DiyCustomWatchFacesData
import com.noisefit_commans.data.model.Feedback
import com.noisefit_commans.data.model.NotificationApp
import com.noisefit_commans.data.model.RandomWatchFaceResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.response.CatWiseWatchFacesItem
import com.noisefit.data.remote.response.DiyMyCreation
import com.noisefit.data.remote.response.WatchFaceCategory2
import com.noisefit.data.remote.response.WatchFaceCustomListResponse
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.ui.watchface2.sub.WF2SubListFrom
import com.noisefit_commans.data.response.*
import com.noisefit_commans.models.Contact
import com.noisefit_commans.models.WatchFace
import kotlinx.coroutines.flow.Flow

interface WatchFaceRepository {


    suspend fun getCloudWatchFaces(deviceType: String): Flow<OfflineResult<List<WatchFace>?>>

    suspend fun getSdkCloudWatchFace(): Flow<CacheResult<List<WatchFace>?>>

    suspend fun saveCloudWatchFace(watchFaceList: List<WatchFace>): Flow<CacheResult<List<WatchFace>?>>

    suspend fun getWatchFaceCategory(isForceRefresh:Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<CatWiseWatchFacesItem>>>>

    suspend fun getWatchFaceCustomData(isForceRefresh: Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<WatchFaceCustomListResponse>>>

    suspend fun getWatchFace2CustomData(
        isForceRefresh: Boolean,
        subListFrom: WF2SubListFrom,
        catId: Int?
    ): Flow<Resource<BaseApiResponse<List<Watchface2>>>>

    suspend fun getWatchfaceCategoriesV2(
        catId: Int? = null, page: Int? = null
    ): Flow<Resource<BaseApiResponse<List<WatchFaceCategory2>>>>

    suspend fun getWatchfaceCategoriesOnlyV2(): Flow<Resource<BaseApiResponse<List<WatchFaceCategory2>>>>

    suspend fun getWatchFacesByCategoryId(
        categoryId: Int
    ): Flow<OfflineResult<List<WatchFace>?>>

    suspend fun getWatchFaceById(watchFaceId: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<WatchFaceResponse>>>

    suspend fun getWatchFaceDownloadInfo(watchFaceId: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<WatchFaceDownloadResponse>>>

    suspend fun markAsFavourite(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<Any>>>

    suspend fun rateWatchFace(requestObject: JsonObject): Flow<Resource<BaseApiResponseData<Any>>>

    suspend fun getFavouriteWatchFaces(isForceRefresh: Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<WatchFace>>>>

    suspend fun getNewlyAddedWatchFaces(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<WatchFace>>>>

    suspend fun getTopDownloadedWatchFaces(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<WatchFace>>>>

    suspend fun setRecentWatchFace(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<String>>>

    suspend fun setWatchFaceDownload(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<String>>>

    suspend fun getRecentWatchFace(): Flow<Resource<BaseApiResponse<List<Watchface2>?>>>

    suspend fun addToFavourites(watchFace: WatchFace)

    suspend fun removeFromFavourites(watchFaceId: Int)

    suspend fun removeAllFavourites()

    suspend fun removeAllWatchfaceData()

    suspend fun getCustomDiyWatchFacesData(): Flow<Resource<BaseApiResponse<DiyCustomWatchFacesData>?>>

    suspend fun createDiyWatchFaceOnline(diyMyCreation: DiyMyCreation): Flow<Resource<BaseApiResponseData<Any>?>>

    suspend fun getDiyWatchFaceOnlineList(): Flow<Resource<BaseApiResponse<List<DiyMyCreation>>?>>

    suspend fun deleteDiyWatchFaceOnline(id: Int): Flow<Resource<BaseApiResponseData<Any>?>>


}