package com.noisefit.data.base

import com.noisefit_commans.data.ErrorResponse

data class DataState<T>(
    var data: T? = null,
    var response: ErrorResponse? = null
) {
    companion object {
        fun <T> error(
            response: ErrorResponse,
        ): DataState<T> {
            return DataState(
                data = null,
                response = response
            )
        }

        fun <T> data(
            response: ErrorResponse?,
            data: T? = null
        ): DataState<T> {
            return DataState(
                response = response,
                data = data
            )
        }
    }
}