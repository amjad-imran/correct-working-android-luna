package com.noisefit.data.remote.abstraction

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface AudioApiService {

    @Streaming
    @GET
    suspend fun getAudioStream(
        @Url url: String,
        @Query("message") message: String
    ): ResponseBody
}