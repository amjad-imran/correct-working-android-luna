package com.noisefit.data.local.db

import com.noisefit_commans.data.ErrorResponse

sealed class CacheResult<out T> {

    data class Success<out T>(val value: T) : CacheResult<T>()

    data class GenericError(
        val errorMessage: String? = null
    ) : CacheResult<Nothing>()
}

sealed class OfflineResult<out T> {

    data class Success<out T>(val value: T) : OfflineResult<T>()
    data class NetworkError(val response: ErrorResponse) : OfflineResult<Nothing>()
    data class Loading(val loading: Boolean) : OfflineResult<Nothing>()
    data class GenericError(
        val errorMessage: String? = null
    ) : OfflineResult<Nothing>()
}