package com.noisefit.data.remote.abstraction

import com.noisefit.data.remote.response.*
import com.noisefit_commans.data.model.Token
import com.noisefit_commans.data.response.BaseApiResponse
import retrofit2.http.*


interface TokenRefreshApi {

    @GET
    suspend fun refreshAccessToken(
        @Url url: String,
        @Header("refresh-token") refreshToken: String,
        @Header("wearable-type") wearableType: String,
        @Header("user-agent") userAgent: String,
    ): BaseApiResponse<Token>

}