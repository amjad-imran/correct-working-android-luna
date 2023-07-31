package com.noisefit.data.local.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect


abstract class CacheResponseHandler<Data>(
    private val response: Flow<CacheResult<Data?>>,
) {
    suspend fun getResult() {
        return response.collect { response ->
            when (response) {
                is CacheResult.Success -> {
                    if (response.value == null) {
                        handleError()
                    } else {
                        handleSuccess(resultObj = response.value)
                    }
                }
                is CacheResult.GenericError -> {

                    handleError()
                }
            }
        }

    }

    abstract suspend fun handleSuccess(resultObj: Data)
    abstract suspend fun handleError()

}