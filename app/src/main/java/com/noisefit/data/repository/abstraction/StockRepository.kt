package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.stock.StockResponse
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun getStockList(
    ): Flow<Resource<StockResponse?>>


    suspend fun searchStock(
        symbol: String,
    ): Flow<Resource<StockResponse?>>


    suspend fun getStocksInfo(
        stocks: String,
    ): Flow<Resource<JsonObject?>>

}