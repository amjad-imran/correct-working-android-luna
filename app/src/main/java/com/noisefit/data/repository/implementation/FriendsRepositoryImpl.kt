package com.noisefit.data.repository.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.noisefit.BuildConfig
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit_commans.data.model.*
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit_commans.ui.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_ACCEPT_STRING
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_REJECTED_STRING
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_REMOVE_STRING
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.response.*
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)


const val FRIENDS_AUTO_REFRESH_MINUTES = 5

const val WF_2_AUTO_REFRESH_MINUTES = 60

class FriendsRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val gson: Gson,
    private val keyValueDataSource: KeyValueDataSource,
    private val lastSyncProvider: LastSyncProvider,
    private val localDatSource: DataStoredInterface,
    private val userActivityRepository: UserActivityRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FriendsRepository {

    override suspend fun getFriendList(
        forceRefresh: Boolean,
        startDate: String,
        endDate: String,
        duration: String
    ): Flow<Resource<BaseApiResponse<FriendsData>>> {
        return flow {
            var resultData: FriendsData? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData(duration, KeyValueDataType.FRIENDS_ACTIVITY)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne() || forceRefresh || lastCallTime.checkDayDifferenceMoreNMinutes(
                        FRIENDS_AUTO_REFRESH_MINUTES
                    )

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(
                        duration,
                        KeyValueDataType.FRIENDS_ACTIVITY
                    )
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<FriendsData>(
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
                val url =
                    "${BuildConfig.BASE_URL_NEW}/friends/list"
                remoteDataSource.getFriendList(url, startDate, endDate)
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
                            key = duration,
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.FRIENDS_ACTIVITY.name
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

    override suspend fun getCommonFriends(request: JsonObject): Flow<Resource<BaseApiResponse<List<BuddiesUserNew>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/common-friends"
            remoteDataSource.getCommonFriends(url, request)
        }
    }

    override suspend fun getPendingRequestCount(): Flow<Resource<BaseApiResponse<RequestCountResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/feed/dashboard/request_count"
            remoteDataSource.getPendingRequestCount(url)
        }
    }

    override suspend fun getFriendProfile(
        forceRefresh: Boolean,
        request: JsonObject,
        profileId: Long
    ): Flow<Resource<BaseApiResponse<FriendProfile>>> {
        return flow {
//            var resultData: FriendProfile? = null

//            val cacheResult = safeCacheCall(Dispatchers.IO) {
//
//                val localData =
//                    keyValueDataSource.getData("$profileId", KeyValueDataType.FRIENDS_PROFILE)
//                        ?: return@safeCacheCall null
//
//                val lastCallTime = localData.getSafeLastSyncValue()
//
//                val shouldCallApi =
//                    lastCallTime.checkDayDifferenceMoreOne() || forceRefresh || lastCallTime.checkDayDifferenceMoreNMinutes(
//                        FRIENDS_AUTO_REFRESH_MINUTES
//                    )
//
//                if (shouldCallApi) {
//                    keyValueDataSource.removeDataByKey(
//                        "$profileId",
//                        KeyValueDataType.FRIENDS_PROFILE
//                    )
//                    return@safeCacheCall null
//                } else {
//
//                    if (localData.value == null) {
//                        return@safeCacheCall null
//                    }
//
//                    return@safeCacheCall localData.value?.let {
//                        Gson().fromJson<FriendProfile>(
//                            it
//                        )
//                    }
//                }
//            }
//            cacheResult.collect { resource ->
//                when (resource) {
//                    is CacheResult.Success -> {
//
//                        resource.value?.let {
//                            resultData = it
//                        }
//                    }
//                    is CacheResult.GenericError -> {
//
//                    }
//                }
//            }
//
//
//            if (resultData != null) {
//                emit(
//                    Resource.Success(
//                        BaseApiResponse(
//                            data = resultData,
//                            message = "",
//                        )
//                    )
//                )
//                return@flow
//            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url =
                    "${BuildConfig.BASE_URL_NEW}/users/profile"
                remoteDataSource.getFriendProfile(url, request)
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
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
                                        data = response,
                                        message = "",
                                    )
                                )
                            )
                        }


                    }
                }
            }

//            if (resultData != null) {
//                safeCacheCall(Dispatchers.IO) {
//                    keyValueDataSource.insertData(
//                        KeyValue(
//                            key = "$profileId",
//                            value = gson.toJson(resultData),
//                            type = KeyValueDataType.FRIENDS_PROFILE.name
//                        )
//                    )
//                }.collect { resource ->
//                    when (resource) {
//                        is CacheResult.Success -> {
//                            emit(
//                                Resource.Success(
//                                    BaseApiResponse(
//                                        data = resultData,
//                                        message = "",
//                                    )
//                                )
//                            )
//                        }
//                        is CacheResult.GenericError -> {
//                            emit(Resource.GenericError(message = "Something went wrong", 0))
//                        }
//                    }
//                }
//            }
        }
    }

    override suspend fun getFriendChallenges(userId: Long): Flow<Resource<BaseApiResponse<ChallengeFriendListingResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/challenges/v2/my_profile/$userId"
            remoteDataSource.getFriendChallenges(url)
        }
    }

    override suspend fun getFriendBadges(request: JsonObject): Flow<Resource<BaseApiResponse<List<FriendBadge>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/users/badges"
            remoteDataSource.getFriendBadges(url, request)
        }
    }

    override suspend fun getCompetitionList(forceRefresh: Boolean): Flow<Resource<BaseApiResponse<CompetitionListResponse>>> {
        return flow {
            var resultData: CompetitionListResponse? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData("0", KeyValueDataType.FRIENDS_COMPETITION)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne() || forceRefresh || lastCallTime.checkDayDifferenceMoreNMinutes(
                        FRIENDS_AUTO_REFRESH_MINUTES
                    )

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(
                        "0",
                        KeyValueDataType.FRIENDS_COMPETITION
                    )
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<CompetitionListResponse>(
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
                val url =
                    "${BuildConfig.BASE_URL_NEW}/friends/competition/list"
                remoteDataSource.getCompetitionList(url)
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
                            key = "0",
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.FRIENDS_COMPETITION.name
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

    override suspend fun getCompeteFriendsList(): Flow<Resource<BaseApiResponse<CompeteFriendResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/compete-friend/list"
            remoteDataSource.getCompetitionFriendList(url)
        }
    }

    override suspend fun getNoiseFitContactsList(request: JsonObject): Flow<Resource<BaseApiResponse<List<BuddiesUserNew>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/check"
            remoteDataSource.getNoiseFitContactsList(url, request)
        }
    }

    override suspend fun getInterestList(request: JsonObject): Flow<Resource<BaseApiResponse<List<BuddiesUserNew>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/check"
            remoteDataSource.getInterestList(url, request)
        }
    }

    override suspend fun getPastWinnerList(request: JsonObject): Flow<Resource<BaseApiResponse<List<BuddiesUserNew>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/check"
            remoteDataSource.getPastWinnerList(url, request)
        }
    }

    override suspend fun getNearBy(request: JsonObject): Flow<Resource<BaseApiResponse<List<BuddiesUserNew>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/check"
            remoteDataSource.getNearByList(url, request)
        }
    }

    override suspend fun getReceivedRequest(): Flow<Resource<BaseApiResponse<ReceivedRequestResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/requests_received"
            remoteDataSource.getReceivedRequest(url)
        }
    }

    override suspend fun getSentRequest(): Flow<Resource<BaseApiResponse<ReceivedRequestResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/requests_sent"
            remoteDataSource.getSentRequest(url)
        }
    }

    override suspend fun getAllCompetitionRequests(): Flow<Resource<BaseApiResponse<List<Requests>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/competition/request-list/received"
            remoteDataSource.getAllCompetitionRequests(url)
        }
    }

    override suspend fun setCompetitionRequestStatus(request: JsonObject): Flow<Resource<BaseApiResponse<String>>> {
        return safeApiCallFlow(dispatcher) {
//friend_id
            LOGS.d("dsadsadsadsadsadsadsads $request")
            if (request.get("request_status").asString.equals(FRIEND_STATUS_REMOVE_STRING) ||
                request.get("request_status").asString.equals(FRIEND_STATUS_ACCEPT_STRING) ||
                request.get("request_status").asString.equals(FRIEND_STATUS_REJECTED_STRING)
            ) {
                keyValueDataSource.removeDataByType(
                    KeyValueDataType.FRIENDS_ACTIVITY
                )
                keyValueDataSource.removeDataByType(
                    KeyValueDataType.FRIENDS_COMPETITION
                )

            }

//            keyValueDataSource.removeDataByKey(
//                request.get("friend_id").asString,
//                KeyValueDataType.FRIENDS_PROFILE
//            )
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/competition/request"
            remoteDataSource.setCompetitionRequestStatus(url, request)
        }
    }

    override suspend fun setFriendRequestStatus(request: JsonObject): Flow<Resource<BaseApiResponse<String>>> {
        return safeApiCallFlow(dispatcher) {

            if (request.get("request_status").asString.equals(FRIEND_STATUS_ACCEPT_STRING) ||
                request.get("request_status").asString.equals(FRIEND_STATUS_REMOVE_STRING) ||
                request.get("request_status").asString.equals(FRIEND_STATUS_REJECTED_STRING)
            ) {
                keyValueDataSource.removeDataByType(
                    KeyValueDataType.FRIENDS_ACTIVITY
                )
                keyValueDataSource.removeDataByType(
                    KeyValueDataType.FRIENDS_COMPETITION
                )
            }
            userActivityRepository.removeLocalStreakData()


//            keyValueDataSource.removeDataByKey(
//                request.get("friend_id").asString,
//                KeyValueDataType.FRIENDS_PROFILE
//            )
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/request"
            remoteDataSource.setFriendRequestStatus(url, request)
        }
    }

    override suspend fun saveUserLocation(request: JsonObject): Flow<Resource<BaseApiResponse<UserLocationUpdatedResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/users/location/save"
            remoteDataSource.saveUserLocation(url, request)
        }
    }

    override suspend fun postEmojiToUser(
        request: JsonObject,
        resetCache: Boolean
    ): Flow<Resource<BaseApiResponse<Any>>> {


        return safeApiCallFlow(dispatcher) {
            if (resetCache) {
                keyValueDataSource.removeDataByType(
                    KeyValueDataType.FRIENDS_ACTIVITY
                )
            }
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/emojis/save"
            remoteDataSource.postEmojiToUser(url, request)
        }
    }

    override suspend fun getStateList(): Flow<Resource<BaseApiResponse<List<StateData>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/master/location/state_list/1"
            remoteDataSource.getStateList(url)
        }
    }

    override suspend fun getUserFriendEmoji(): Flow<Resource<BaseApiResponse<UserFriendReactions>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/friends/emojis/list"
            remoteDataSource.getUserFriendEmoji(url)
        }
    }

    override suspend fun getCityList(stateId: Int): Flow<Resource<BaseApiResponse<List<CityData>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/master/location/city_list/$stateId"
            remoteDataSource.getCityList(url)
        }
    }

    override suspend fun updateEmojiLocally(emoji: String?, profileId: Long) {
        with(Dispatchers.IO) {


            keyValueDataSource.removeDataByType(KeyValueDataType.FRIENDS_ACTIVITY)

            val localData =
                keyValueDataSource.getData("$profileId", KeyValueDataType.FRIENDS_PROFILE)
                    ?: return

            keyValueDataSource.removeDataByKey(
                "$profileId",
                KeyValueDataType.FRIENDS_PROFILE
            )

            val res = localData.value?.let {
                Gson().fromJson<FriendProfile>(
                    it
                )
            }
            res?.user_emoji = emoji

            res?.let {

                keyValueDataSource.insertData(
                    KeyValue(
                        key = "$profileId",
                        value = gson.toJson(it),
                        type = KeyValueDataType.FRIENDS_PROFILE.name
                    )
                )

            }

        }
    }

    private fun shouldCalRoundUpApi(
        serverTime: Long,
        localTime: Long
    ): Boolean {
        if (serverTime == 0L) return true
        if (localTime == 0L) return true

        return localTime < serverTime
    }

    fun getLocalRoundUpData(removeData: Boolean): RoundUpResponse? {
        if (removeData) {
            localDatSource.setRoundUpData(null)
            return null
        }
        return localDatSource.getRoundUpData()
    }

    override suspend fun getYearlySummary(): Flow<Resource<BaseApiResponse<RoundUpResponse>>> {
        return flow {

            val serverUpdateTimeStamp =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.SUMMARY_SERVER_TIMESTAMP)
            val localSyncTime =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.SUMMARY_LOCAL_TIMESTAMP)

            val shouldCallApi = shouldCalRoundUpApi(serverUpdateTimeStamp, localSyncTime)

            var resultData: RoundUpResponse? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {
                getLocalRoundUpData(shouldCallApi)
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
                emit(Resource.Success(
                    BaseApiResponse(
                        data = resultData,
                        message = ""
                    )
                ))
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url =
                    "${BuildConfig.BASE_URL_NEW}/app_summary/yearly_summary"
                remoteDataSource.getAllSummaryData(url)
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
                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.SUMMARY_LOCAL_TIMESTAMP)
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    localDatSource.setRoundUpData(resultData)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Success(
                                BaseApiResponse(
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


}