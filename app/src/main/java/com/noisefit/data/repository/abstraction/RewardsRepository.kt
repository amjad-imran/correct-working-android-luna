package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit_commans.data.model.*
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.*
import kotlinx.coroutines.flow.Flow

interface RewardsRepository {

    suspend fun getDashboardRewardsData(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<DashboardRewardsResponse>>>

    suspend fun collectPoints(
        requestObject: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<CollectCoinResponse>>>


    suspend fun getRewardsProfile(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<Any>>>

    suspend fun earnRewardsPoints(
        requestObject: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<Any>>>


    suspend fun getTransactionHistory(
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<TransHistoryData>>>

    suspend fun getTaskList(
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<TaskListData>>>

    suspend fun getRewardProfileData(
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<RewardProfileData>>>

    suspend fun getDealsList(
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.AllDealsResponse>>>

    suspend fun getStreakData(
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<StreakDetailsResponse>>>

    suspend fun getRewardAboutList(
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<RewardAboutData>>>>

    suspend fun getStreaksAboutData(
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.AboutStreakResponse>>>

    suspend fun availCoupon(couponId: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.model.AvailCouponData>>>

    suspend fun getVoucherList(
        flagType: String,
        forceRefresh: Boolean
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VoucherListData>>>

    suspend fun getVoucherDetails(
        id: Int,
        forceRefresh: Boolean
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VoucherDetailsData>>>

    suspend fun getCouponDetails(id: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VoucherDetailsData>>>


}