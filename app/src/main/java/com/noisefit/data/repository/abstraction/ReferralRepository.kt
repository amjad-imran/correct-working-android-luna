package com.noisefit.data.repository.abstraction

import com.noisefit.data.model.referral.ReferralCodeResponse
import com.noisefit.data.model.referral.ReferralInfoResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.model.referral.ReferralsMain
import kotlinx.coroutines.flow.Flow

interface ReferralRepository {

    suspend fun getReferCode(): Flow<Resource<BaseApiResponse<ReferralCodeResponse>>>

    suspend fun getReferralInfo(): Flow<Resource<BaseApiResponse<ReferralInfoResponse>>>

    suspend fun getReferralHistory(): Flow<Resource<BaseApiResponse<List<ReferralsMain>>>>

}