package com.noisefit.data.repository.implementation

import com.google.gson.JsonObject
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.NetworkConstants
import com.noisefit.data.remote.NetworkErrors
import com.noisefit.data.remote.abstraction.StockService
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.stock.StockResponse
import com.noisefit.data.repository.abstraction.StockRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException

const val STOCK_API_KEY = "6ea360120fe64dffa00d78f19488e61f"
class StockRepositoryImpl(
    private val stockDataSource: StockService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StockRepository {


    override suspend fun getStockList(): Flow<Resource<StockResponse?>> {
        return safeApiCallFlowStock(dispatcher){
            stockDataSource.getStockList("IND",STOCK_API_KEY)
        }
    }

    override suspend fun searchStock(symbol: String): Flow<Resource<StockResponse?>> {
        return safeApiCallFlowStock(dispatcher){
            stockDataSource.searchStock(symbol, STOCK_API_KEY)
        }
    }

    override suspend fun getStocksInfo(stocks: String): Flow<Resource<JsonObject?>> {
        return safeApiCallFlowStock(dispatcher){
            stockDataSource.getStocksInfo(stocks, STOCK_API_KEY)
        }
    }

}

suspend fun <T> safeApiCallFlowStock(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): Flow<Resource<T>> {

    return flow {
        emit(Resource.Loading(true))
        try {
            withTimeout(NetworkConstants.CALL_TIMEOUT) {
                val response = apiCall.invoke()
                emit(Resource.Success(response))
                emit(Resource.Loading(false))
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is TimeoutCancellationException -> {
                    emit(networkError())
                }
                is IOException -> {
                    emit(networkError())
                }
                is HttpException -> {
                    emit(networkError())
                }
                else -> {
                    emit(networkError())
                }
            }
        }
    }.flowOn(dispatcher).catch {  }
}

private fun networkError(): Resource.NetworkError {
    return Resource.NetworkError(
        ErrorResponse(
            UIComponentType.RetryApiDialog(NetworkErrors.NETWORK_ERROR_UNKNOWN)
        )
    )
}