package com.noisefit.data.repository.implementation

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.noisefit.BuildConfig
import com.noisefit.data.local.db.CacheErrors
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.OfflineResult
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.local.db.implementation.WatchFaceDataSourceImpl
import com.noisefit.data.model.diy.DiyCustomWatchFacesData
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.response.CatWiseWatchFacesItem
import com.noisefit.data.remote.response.DiyMyCreation
import com.noisefit.data.remote.response.WatchFaceCategory2
import com.noisefit.data.remote.response.WatchFaceCustomListResponse
import com.noisefit.data.remote.response.Watchface2
import com.noisefit_commans.data.response.*
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.WatchFaceRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit.ui.watchface2.sub.WF2SubListFrom
import com.noisefit_commans.data.model.KeyValue
import com.noisefit_commans.models.FavouriteWatchFace
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.ui.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import com.noisefit_commans.ui.isValidUrl
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanNDays
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)
class WatchFaceRepositoryImpl(
    private val gson: Gson,
    private val keyValueDataSource: KeyValueDataSource,
    private val remoteDataSource: NetworkService,
    private val lastSyncProvider: LastSyncProvider,
    private val watchFaceDataImpl: WatchFaceDataSourceImpl,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : WatchFaceRepository {


    override suspend fun getCloudWatchFaces(deviceType: String): Flow<OfflineResult<List<WatchFace>?>> {
        return flow {


            //search for offline faces
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                watchFaceDataImpl.getWatchFaces()
            }

            val watchFaceList = ArrayList<WatchFace>()
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            watchFaceList.addAll(it)

                        }


                    }

                    is CacheResult.GenericError -> {
                        emit(OfflineResult.GenericError(CacheErrors.CACHE_ERROR))
                        LOGS.e("getCloudWatchFacesNew", "watch faces offline Error")

                    }
                }
            }


            //else search for data online

            if (watchFaceList.isNotEmpty()) {
                emit(OfflineResult.Success(watchFaceList))
                return@flow
            }
            LOGS.e("getCloudWatchFacesNew", "searching for online")
            val serverResult = safeApiCallFlow(dispatcher) {
                val url = "${BuildConfig.BASE_URL_NEW}/watch_faces/in_built/list"
                remoteDataSource.getCloudWatchFaces(url)
            }

            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(OfflineResult.GenericError(resource.message))
                    }
                    is Resource.Loading -> {
                        emit(OfflineResult.Loading(resource.loading))
                    }
                    is Resource.NetworkError -> {
                        emit(OfflineResult.NetworkError(resource.response))
                    }
                    is Resource.Success -> {
                        LOGS.e("getCloudWatchFacesNew", "got data online")
                        resource.data?.data?.let { response ->
                            val facesList = response.filter {
                                if (!it.imageUrl.isNullOrEmpty()) {
                                    it.faceId = "${it.id}-${it.imageType}"
                                    true
                                } else {
                                    false
                                }
                            }

                            watchFaceList.addAll(facesList)

                        }
                    }
                }
            }



            if (watchFaceList.isNotEmpty()) {
                LOGS.e("getCloudWatchFacesNew", "saving data online")
                safeCacheCall(Dispatchers.IO) {
                    watchFaceDataImpl.insertData(watchFaceList)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(OfflineResult.Success(watchFaceList))
                        }
                        is CacheResult.GenericError -> {
                            emit(OfflineResult.GenericError(CacheErrors.CACHE_ERROR))
                        }
                    }
                }

            }
        }

    }

    override suspend fun getSdkCloudWatchFace(): Flow<CacheResult<List<WatchFace>?>> {
        return safeCacheCall(Dispatchers.IO) {
            watchFaceDataImpl.getWatchFaces()
        }
    }

    override suspend fun saveCloudWatchFace(watchFaceList: List<WatchFace>): Flow<CacheResult<List<WatchFace>?>> {
        return safeCacheCall(Dispatchers.IO) {
            watchFaceDataImpl.insertData(watchFaceList)
        }
    }

    override suspend fun getWatchFaceCategory(isForceRefresh: Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<CatWiseWatchFacesItem>>>> {

        return flow {
            val shouldCallApi =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.WATCHFACE_CATEGORIES)
                    .checkTimeDifferenceMoreThanNDays(1) || isForceRefresh

            val resultData = ArrayList<CatWiseWatchFacesItem>()
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                watchFaceDataImpl.getWatchFaceCategories(shouldCallApi)
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData.addAll(it)
                        }
                    }
                    is CacheResult.GenericError -> {
                        LOGS.e("getFavouriteWatchFaces", "watch faces offline Error")
                    }
                }
            }


            if (resultData.isNotEmpty()) {
                emit(Resource.Success(
                    com.noisefit_commans.data.response.BaseApiResponse(
                        data = resultData,
                        message = ""
                    )
                ))
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url = BuildConfig.BASE_URL_NEW + "/watch_faces/list_by_category"
                remoteDataSource.getWatchFaceCategory(url)
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }
                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }
                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }
                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.WATCHFACE_CATEGORIES)
                            resultData.clear()
                            resultData.addAll(response)
                        }
                    }
                }
            }

            if (resultData.isNotEmpty()) {
                safeCacheCall(Dispatchers.IO) {
                    watchFaceDataImpl.setWatchFaceCategories(resultData)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Success(
                                com.noisefit_commans.data.response.BaseApiResponse(
                                    data = resultData,
                                    message = ""
                                )
                            ))
                        }
                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }

    }

    override suspend fun getWatchFaceCustomData(isForceRefresh: Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<WatchFaceCustomListResponse>>> {
        return flow {

            val shouldCallApi =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.WATCHFACE_MAIN_LIST)
                    .checkTimeDifferenceMoreThanNDays(1) || isForceRefresh


            var customData: WatchFaceCustomListResponse? = null
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                watchFaceDataImpl.getCustomWatchFaceData(shouldCallApi)
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            customData = it
                        }
                    }
                    is CacheResult.GenericError -> {
                        LOGS.e("getFavouriteWatchFaces", "watch faces offline Error")
                    }
                }
            }


            if (customData != null) {
                emit(Resource.Success(
                    com.noisefit_commans.data.response.BaseApiResponse(
                        data = customData,
                        message = ""
                    )
                ))
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url = BuildConfig.BASE_URL_NEW + "/watch_faces/list"
                remoteDataSource.getWatchFaceCustomData(url)
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }
                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }
                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }
                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            safeCacheCall(Dispatchers.IO) {
                                watchFaceDataImpl.deleteWatchFacesByCategoryId(-2)//Newly added
                                watchFaceDataImpl.deleteWatchFacesByCategoryId(-3)//Top downloaded
                            }.collect()

                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.WATCHFACE_MAIN_LIST)
                            customData = response
                        }
                    }
                }
            }

            if (customData != null) {

                safeCacheCall(Dispatchers.IO) {
                    watchFaceDataImpl.setCustomWatchFaceData(customData)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Success(
                                com.noisefit_commans.data.response.BaseApiResponse(
                                    data = customData,
                                    message = ""
                                )
                            ))
                        }
                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }

    }

    override suspend fun getWatchfaceCategoriesV2(
        catId: Int?,
        page: Int?
    ): Flow<Resource<BaseApiResponse<List<WatchFaceCategory2>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                BuildConfig.BASE_URL_NEW + "/watch_faces/v2/profile"
            remoteDataSource.getWatchfaceCategoriesV2(url, catId, page)
        }

    }

    override suspend fun getWatchfaceCategoriesOnlyV2(): Flow<Resource<BaseApiResponse<List<WatchFaceCategory2>>>> {//Flow<Resource<BaseApiResponse<FriendsData>>>

        return flow {
            var resultData: List<WatchFaceCategory2>? = null

            val key = KeyValueDataType.WATCH_FACE_2_CATEGORY_LIST.name
            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData(key, KeyValueDataType.WATCH_FACE_2_CATEGORY_LIST)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne() || lastCallTime.checkDayDifferenceMoreNMinutes(
                        WF_2_AUTO_REFRESH_MINUTES
                    )

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(key, KeyValueDataType.WATCH_FACE_2_CATEGORY_LIST)
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<List<WatchFaceCategory2>>(
                            it
                        )
                    }
                }
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData = it
                        }
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }


            if (resultData != null) {
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url = BuildConfig.BASE_URL_NEW + "/watch_faces/v2/category/list"
                remoteDataSource.getWatchfaceCategoriesV2(url)
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }

                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }

                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }

                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = key,
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.WATCH_FACE_2_CATEGORY_LIST.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
                                        data = resultData,
                                        message = "",
                                    )
                                )
                            )
                        }

                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }


    }

    override suspend fun getWatchFace2CustomData(
        isForceRefresh: Boolean,
        subListFrom: WF2SubListFrom,
        catId: Int?
    ): Flow<Resource<BaseApiResponse<List<Watchface2>>>> {

        return flow {
            var mIsForceApp = isForceRefresh
            var resultData: List<Watchface2>? = null

            val key = when (subListFrom) {
                WF2SubListFrom.Newly -> {
                    "${KeyValueDataType.WATCH_FACE_2.name}_newly_added_list"
                }

                WF2SubListFrom.Popular -> {
                    "${KeyValueDataType.WATCH_FACE_2.name}_popular_list"
                }

                WF2SubListFrom.Trending -> {
                    "${KeyValueDataType.WATCH_FACE_2.name}_trending"
                }


                WF2SubListFrom.CATEGORIES -> {
                    "${KeyValueDataType.WATCH_FACE_2.name}_$catId"
                }

                else -> {
                    ""
                }
            }

            if(key.isEmpty()){
                mIsForceApp = true
            }

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData(key, KeyValueDataType.WATCH_FACE_2)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne() || mIsForceApp || lastCallTime.checkDayDifferenceMoreNMinutes(
                        WF_2_AUTO_REFRESH_MINUTES
                    )

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(key, KeyValueDataType.WATCH_FACE_2)
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<List<Watchface2>>(
                            it
                        )
                    }
                }
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData = it
                        }
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }


            if (resultData != null) {
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url = when (subListFrom) {
                    WF2SubListFrom.Newly -> {
                        BuildConfig.BASE_URL_NEW + "/watch_faces/v2/newly_added_list"
                    }

                    WF2SubListFrom.Popular -> {
                        BuildConfig.BASE_URL_NEW + "/watch_faces/v2/popular_list"
                    }

                    WF2SubListFrom.Trending -> {
                        BuildConfig.BASE_URL_NEW + "/watch_faces/v2/trending"
                    }

                    WF2SubListFrom.CATEGORIES -> {
                        BuildConfig.BASE_URL_NEW + "/watch_faces/v2/watchfaces_by_category/$catId"
                    }

                    WF2SubListFrom.Favourite -> {
                        BuildConfig.BASE_URL_NEW + "/watch_faces/v2/my_favourites"
                    }

                    else -> {
                        BuildConfig.BASE_URL_NEW + "/watch_faces/v2/newly_added_list"
                    }
                }

                remoteDataSource.getWatchFace2CustomData(url)
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }

                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }

                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }

                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = key,
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.WATCH_FACE_2.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
                                        data = resultData,
                                        message = "",
                                    )
                                )
                            )
                        }

                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }


    }

    override suspend fun getWatchFacesByCategoryId(
        categoryId: Int
    ): Flow<OfflineResult<List<WatchFace>?>> {

        return flow {


            //search for offline faces
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                watchFaceDataImpl.getWatchFacesByCategory(categoryId)
            }
            val watchFaceList = ArrayList<WatchFace>()
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            watchFaceList.addAll(it)
                        }
                    }
                    is CacheResult.GenericError -> {
                        emit(OfflineResult.GenericError(CacheErrors.CACHE_ERROR))
                    }
                }
            }

            if (watchFaceList.isNotEmpty()) {
                emit(OfflineResult.Success(watchFaceList))
                return@flow
            }
            val serverResult = safeApiCallFlow(dispatcher) {
                val url = when (categoryId) {
                    -2 -> BuildConfig.BASE_URL_NEW + "/watch_faces/newly_added_list"
                    -3 -> BuildConfig.BASE_URL_NEW + "/watch_faces/top_downloaded_list"
                    else -> BuildConfig.BASE_URL_NEW + "/watch_faces/v2/watchfaces_by_category/$categoryId"
                }

                remoteDataSource.getWatchFacesByCategoryId(url)
            }

            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(OfflineResult.GenericError(resource.message))
                    }
                    is Resource.Loading -> {
                        emit(OfflineResult.Loading(resource.loading))
                    }
                    is Resource.NetworkError -> {
                        emit(OfflineResult.NetworkError(resource.response))
                    }
                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            watchFaceList.addAll(response)
                            watchFaceList.forEach {
                                it.watchfaceCatId = categoryId
                            }
                        }
                    }
                }
            }

            if (watchFaceList.isNotEmpty()) {

                safeCacheCall(Dispatchers.IO) {
                    watchFaceDataImpl.insertData(watchFaceList)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(OfflineResult.Success(watchFaceList))
                        }
                        is CacheResult.GenericError -> {
                            emit(OfflineResult.GenericError(CacheErrors.CACHE_ERROR))
                        }
                    }
                }

            }


        }


    }

    override suspend fun getWatchFaceById(watchFaceId: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<WatchFaceResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/detail/$watchFaceId"
            remoteDataSource.getWatchFaceById(url)
        }
    }

    override suspend fun getWatchFaceDownloadInfo(watchFaceId: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<WatchFaceDownloadResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getWatchFaceDownloadInfo(watchFaceId)
        }
    }

    override suspend fun markAsFavourite(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<Any>>> {
        return safeApiCallFlow(dispatcher) {

            val watchFaceId = requestObject.get("watchface_id").asInt
            val isFavourite = requestObject.get("is_favourite").asBoolean

            watchFaceDataImpl.setFavourite(watchFaceId, if (isFavourite) "1" else "0")

            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/favourites"
            remoteDataSource.markAsFavourite(url, requestObject)
        }
    }

    override suspend fun rateWatchFace(requestObject: JsonObject): Flow<Resource<BaseApiResponseData<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/v2/rating"
            remoteDataSource.rateWatchFace(url, requestObject)
        }
    }

    override suspend fun getFavouriteWatchFaces(isForceRefresh: Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<WatchFace>>>> {


        return flow {

            val shouldCallApi =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.FAVOURITES_WATCHFACE)
                    .checkTimeDifferenceMoreThanNDays(1) || isForceRefresh


            val watchFaceList = ArrayList<WatchFace>()
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                watchFaceDataImpl.getFavouriteWatchFaces(shouldCallApi)
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            watchFaceList.addAll(it)
                        }

                        LOGS.e(
                            "getFavouriteWatchFaces",
                            "watch faces offline ${resource.value?.size}"
                        )
                    }
                    is CacheResult.GenericError -> {
                        LOGS.e("getFavouriteWatchFaces", "watch faces offline Error")
                    }
                }
            }


            if (watchFaceList.isNotEmpty()) {
                emit(Resource.Success(com.noisefit_commans.data.response.BaseApiResponseData(data = watchFaceList)))
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url = BuildConfig.BASE_URL_NEW + "/watch_faces/my_favourites"
                remoteDataSource.getFavouriteWatchFaces(url)
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }
                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }
                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }
                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.FAVOURITES_WATCHFACE)
                            watchFaceList.addAll(response)
                            LOGS.e(
                                "getFavouriteWatchFaces",
                                "got data online ${watchFaceList.size}"
                            )
                        }
                    }
                }
            }

            if (watchFaceList.isNotEmpty()) {

                safeCacheCall(Dispatchers.IO) {

                    val favouriteData = ArrayList<FavouriteWatchFace>()
                    watchFaceList.forEach {
                        val wf = FavouriteWatchFace(
                            imageUrl = it.imageUrl,
                            id = it.id,
                            name = it.name,
                            downloads = it.downloads,
                            is_favourite = it.is_favourite
                        )
                        favouriteData.add(wf)
                    }

                    watchFaceDataImpl.insertFavouriteData(favouriteData)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Success(
                                com.noisefit_commans.data.response.BaseApiResponseData(
                                    data = watchFaceList
                                )
                            ))
                        }
                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }

            } else {
                emit(Resource.Success(com.noisefit_commans.data.response.BaseApiResponseData(data = watchFaceList)))
            }
        }
    }

    override suspend fun getNewlyAddedWatchFaces(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<WatchFace>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/newly_added_list"
            remoteDataSource.getNewlyAddedWatchFaces(url)
        }
    }

    override suspend fun getTopDownloadedWatchFaces(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<WatchFace>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/top_downloaded_list"
            remoteDataSource.getTopDownloadedWatchFaces(url)
        }
    }

    override suspend fun setRecentWatchFace(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<String>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.setRecentWatchFace(requestObject)
        }
    }

    override suspend fun setWatchFaceDownload(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<String>>> {
        return safeApiCallFlow(dispatcher) {
            lastSyncProvider.removeSyncTimeStamp(LastSyncItems.WATCHFACE_MAIN_LIST)
            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/downloads"
            remoteDataSource.setWatchFaceDownload(url, requestObject)
        }
    }

    override suspend fun getRecentWatchFace(): Flow<Resource<BaseApiResponse<List<Watchface2>?>>> {
        return safeApiCallFlow(dispatcher) {
            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/random_list"
            remoteDataSource.getRecentWatchFace(url)
        }
    }

    override suspend fun getCustomDiyWatchFacesData(): Flow<Resource<BaseApiResponse<DiyCustomWatchFacesData>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/custom_watchface/list"
            remoteDataSource.getDiyCustomWatchFaces(url)
        }
    }

    override suspend fun createDiyWatchFaceOnline(diyMyCreation: DiyMyCreation): Flow<Resource<BaseApiResponseData<Any>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/v2/save/custom_watchface"
            LOGS.d("createDiyWatchFaceOnline inside")
            if (!diyMyCreation.backgroundUrl.isValidUrl()) {
                LOGS.d("createDiyWatchFaceOnline valid")
                val file = File(Uri.parse(diyMyCreation.backgroundUrl).path!!)
                val requestFile = MultipartBody.Part.createFormData(
                    "background_url",
                    file.name,
                    file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                remoteDataSource.createDiyWatchFaceOnline(
                    url,
                    RequestBody.create("text/plain".toMediaTypeOrNull(), diyMyCreation.filter),
                    RequestBody.create("text/plain".toMediaTypeOrNull(), diyMyCreation.filterIntensity.toString()),
                    RequestBody.create("text/plain".toMediaTypeOrNull(), diyMyCreation.textLayerName),
                    RequestBody.create("text/plain".toMediaTypeOrNull(), diyMyCreation.textLayerLink),
                    RequestBody.create("text/plain".toMediaTypeOrNull(), diyMyCreation.textBin),
                    RequestBody.create("text/plain".toMediaTypeOrNull(), diyMyCreation.textType),
                    RequestBody.create("text/plain".toMediaTypeOrNull(), diyMyCreation.colour),
                    requestFile,
                )
            } else {
                LOGS.d("createDiyWatchFaceOnline not valid")

                val requestObject = JsonObject().apply {
                    addProperty("filter", diyMyCreation.filter)//filter name
                    addProperty("filter_intensity", diyMyCreation.filterIntensity.toString())//yes
                    addProperty("text_layer_name", diyMyCreation.textLayerName)
                    addProperty("text_layer_link", diyMyCreation.textLayerLink)//yes
                    addProperty("text_bin", diyMyCreation.textBin)//yes
                    addProperty("text_type", diyMyCreation.textType)
                    addProperty("colour", diyMyCreation.colour)//yes
                    addProperty("background_url", diyMyCreation.backgroundUrl)//yes
                }
                remoteDataSource.createDiyWatchFaceOnlineWM(url, requestObject)
            }

        }
    }

    override suspend fun getDiyWatchFaceOnlineList(): Flow<Resource<BaseApiResponse<List<DiyMyCreation>>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/v2/custom_watchface/my_creations"
            remoteDataSource.getDiyWatchFaceOnlineList(url)
        }
    }

    override suspend fun deleteDiyWatchFaceOnline(id: Int): Flow<Resource<BaseApiResponseData<Any>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/v2/my_creations/delete/$id"
            remoteDataSource.deleteDiyWatchFaceOnline(url)
        }
    }

    override suspend fun addToFavourites(watchFace: WatchFace) {
        watchFaceDataImpl.addToFavourites(
            FavouriteWatchFace(
                imageUrl = watchFace.imageUrl,
                id = watchFace.id,
                downloads = watchFace.downloads,
                name = watchFace.name,
                is_favourite = "1"
            )
        )
    }

    override suspend fun removeFromFavourites(watchFaceId: Int) {
        watchFaceDataImpl.removeFromFavourites(watchFaceId)
    }

    override suspend fun removeAllFavourites() {
        watchFaceDataImpl.deleteFavouriteWatchFaces()
    }

    override suspend fun removeAllWatchfaceData() {
        watchFaceDataImpl.deleteOldWatchFaces()
        watchFaceDataImpl.deleteFavouriteWatchFaces()
        watchFaceDataImpl.setWatchFaceCategories(null)
        watchFaceDataImpl.setCustomWatchFaceData(null)
        lastSyncProvider.removeSyncTimeStamp(
            listOf(
                LastSyncItems.WATCHFACE_CATEGORIES,
                LastSyncItems.WATCHFACE_MAIN_LIST,
                LastSyncItems.FAVOURITES_WATCHFACE
            )
        )
    }
}