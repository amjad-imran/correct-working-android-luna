package com.noisefit.data.remote.abstraction

import com.google.gson.JsonObject
import com.noisefit_commans.data.response.stock.StockResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface StockService {

    @GET("/stocks")
    suspend fun getStockList(
        @Query("country") country: String,
        @Query("apikey") apiKey: String
    ): StockResponse

    @GET("/symbol_search")
    suspend fun searchStock(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): StockResponse


    @GET("/quote")
    suspend fun getStocksInfo(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): JsonObject



}