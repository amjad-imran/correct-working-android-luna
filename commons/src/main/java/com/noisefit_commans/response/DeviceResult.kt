package com.noisefit_commans.response


sealed class DeviceResult<out T> {

    data class Success<out T>(val value: T) : DeviceResult<T>()

    data class GenericError(
        val errorMessage: String? = null
    ) : DeviceResult<Nothing>()
}