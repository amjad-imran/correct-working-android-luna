package com.noisefit.data.remote.abstraction

import com.google.gson.JsonObject
import com.noisefit_commans.data.response.MessageResponse
import retrofit2.http.Body
import retrofit2.http.GET


interface ExternalFirmwareService {

    @GET("/device/queryFirmwareVersion")
    suspend fun getNavFirmware(
        @Body requestObject: JsonObject
    ): MessageResponse

    @GET("/account/login")
    suspend fun getAccessToken(
        @Body requestObject: JsonObject
    ): MessageResponse

}