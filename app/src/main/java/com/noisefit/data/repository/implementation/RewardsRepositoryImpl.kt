package com.noisefit.data.repository.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.noisefit.BuildConfig
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit_commans.data.model.*
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.*
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit_commans.ui.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)


class RewardsRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val keyValueDataSource: KeyValueDataSource,
    private val userActivityRepository: UserActivityRepository,
    private val gson: Gson,

    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RewardsRepository {

    override suspend fun getDashboardRewardsData(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<DashboardRewardsResponse>>> {
        return flow {
            var resultData: DashboardRewardsResponse? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData = keyValueDataSource.getData("sad", KeyValueDataType.DASH_STREAK_DATA)
                    ?: return@safeCacheCall null

//                val localData = keyValueDataSource.getData("0", KeyValueDataType.DASH_STREAK_DATA)
//                    ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne() || lastCallTime.checkDayDifferenceMoreNMinutes(
                        5
                    )

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(
                        "asdqwe", KeyValueDataType.DASH_STREAK_DATA
                    )
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<DashboardRewardsResponse>(
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
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getDashboardRewardsData(
                    "${BuildConfig.BASE_URL_NEW}/rewards/user/dashboard"
                )
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
                            type = "KeyValueDataType.DASH_STREAK_DATA.namedsasad"
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    com.noisefit_commans.data.response.BaseApiResponse(
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

    override suspend fun collectPoints(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<CollectCoinResponse>>> {
        return safeApiCallFlow(dispatcher) {
            userActivityRepository.removeLocalStreakData()
            remoteDataSource.collectRewardsPoints(
                "${BuildConfig.BASE_URL_NEW}/rewards/user/collect", requestObject
            )
        }
    }

    override suspend fun getRewardsProfile(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getRewardsProfile(
                "${BuildConfig.BASE_URL_NEW}/rewards/user/profile"
            )
        }
    }

    override suspend fun earnRewardsPoints(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            userActivityRepository.removeLocalStreakData()
            remoteDataSource.earnRewardsPoints(
                "${BuildConfig.BASE_URL_NEW}/rewards/user/ui/earn", requestObject
            )
        }
    }

    override suspend fun getTransactionHistory(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<TransHistoryData>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getTransactionHistory("${BuildConfig.BASE_URL_NEW}/rewards/user/points_history")
        }
    }

    override suspend fun getTaskList(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<TaskListData>>> {
        return flow {
            var resultData: TaskListData? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData = keyValueDataSource.getData("0", KeyValueDataType.ALL_TASK_LIST)
                    ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne() || lastCallTime.checkDayDifferenceMoreNMinutes(
                        5
                    )

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(
                        "0", KeyValueDataType.ALL_TASK_LIST
                    )
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<TaskListData>(
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
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getTaskListData("${BuildConfig.BASE_URL_NEW}/rewards/v2/user/task/list")
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
                            type = KeyValueDataType.ALL_TASK_LIST.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    com.noisefit_commans.data.response.BaseApiResponse(
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

    override suspend fun getRewardAboutList(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<RewardAboutData>>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getRewardAboutData("${BuildConfig.BASE_URL_NEW}/rewards/about")
        }
    }

    override suspend fun getStreaksAboutData(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.AboutStreakResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getStreaksAboutData("${BuildConfig.BASE_URL_NEW}/streaks/about/streak")
        }
    }

    override suspend fun availCoupon(couponId: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.model.AvailCouponData>>> {
        return safeApiCallFlow(dispatcher) {
            keyValueDataSource.removeDataByKey(
                "active", KeyValueDataType.USER_COUPON_LIST
            )
            userActivityRepository.removeLocalStreakData()

            remoteDataSource.availCoupon("${BuildConfig.BASE_URL_NEW}/rewards/coupon/redeem/$couponId")
        }
    }

    override suspend fun getRewardProfileData(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<RewardProfileData>>> {
        return flow {
            var resultData: RewardProfileData? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData = keyValueDataSource.getData("0", KeyValueDataType.COINS_PROFILE_DATA)
                    ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne() || lastCallTime.checkDayDifferenceMoreNMinutes(
                        5
                    )

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(
                        "0", KeyValueDataType.COINS_PROFILE_DATA
                    )
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<RewardProfileData>(
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
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getRewardProfileData("${BuildConfig.BASE_URL_NEW}/rewards/v2/user/profile")
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
                            type = KeyValueDataType.COINS_PROFILE_DATA.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    com.noisefit_commans.data.response.BaseApiResponse(
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

    override suspend fun getDealsList(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.AllDealsResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getDealsList("${BuildConfig.BASE_URL_NEW}/rewards/coupon/list")
        }
    }

    override suspend fun getStreakData(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<StreakDetailsResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getStreakData("${BuildConfig.BASE_URL_NEW}/streaks/user_profile")
        }
    }

    override suspend fun getVoucherList(
        flagType: String, forceRefresh: Boolean
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VoucherListData>>> {
        return flow {
            var resultData: VoucherListData? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData("$flagType", KeyValueDataType.USER_COUPON_LIST)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi = lastCallTime.checkDayDifferenceMoreOne() || forceRefresh

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(
                        "$flagType", KeyValueDataType.USER_COUPON
                    )
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<VoucherListData>(
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
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getVoucherList("${BuildConfig.BASE_URL_NEW}/rewards/user/voucher/list/$flagType")
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
                            key = "$flagType",
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.USER_COUPON_LIST.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    com.noisefit_commans.data.response.BaseApiResponse(
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

    override suspend fun getVoucherDetails(
        id: Int, forceRefresh: Boolean
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VoucherDetailsData>>> {
        return flow {
            var resultData: VoucherDetailsData? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData = keyValueDataSource.getData("$id", KeyValueDataType.USER_COUPON)
                    ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi = lastCallTime.checkDayDifferenceMoreOne() || forceRefresh

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(
                        "$id", KeyValueDataType.USER_COUPON
                    )
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<VoucherDetailsData>(
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
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getVoucherDetails("${BuildConfig.BASE_URL_NEW}/rewards/user/voucher/$id")
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
                            key = "$id",
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.USER_COUPON.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    com.noisefit_commans.data.response.BaseApiResponse(
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

    override suspend fun getCouponDetails(id: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VoucherDetailsData>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getCouponDetails("${BuildConfig.BASE_URL_NEW}/rewards/coupon/$id")
        }
    }


}