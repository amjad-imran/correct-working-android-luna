package com.noisefit.data.repository.implementation

import com.noisefit.data.model.referral.ReferralCodeResponse
import com.noisefit.data.model.referral.ReferralInfoResponse
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.ReferralRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.luna.BuildConfig
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.model.referral.ReferralsMain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class ReferralRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReferralRepository {
    override suspend fun getReferCode(): Flow<Resource<BaseApiResponse<ReferralCodeResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/subscription-center/referral-code/claim"
            remoteDataSource.getReferCode(url)
        }
    }

    override suspend fun getReferralInfo(): Flow<Resource<BaseApiResponse<ReferralInfoResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/subscription-center/refer-now"
            remoteDataSource.getReferralInfo(url)
        }
    }

    override suspend fun getReferralHistory(): Flow<Resource<BaseApiResponse<List<ReferralsMain>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/subscription-center/referral-history"
            remoteDataSource.getReferralHistory(url)
        }
    }

}