package com.noisefit.data.remote.base

import com.noisefit_commans.data.ErrorResponse

/**
 * GenericError -> unrecoverable
 * NetworkError -> Recoverable errors, show retry
 */
sealed class Resource<out T> {
    class Success<out T>(val data: T?) : Resource<T>()
    class GenericError(val message: String?, val errorCode: Int?) : Resource<Nothing>()
    class NetworkError(val response: ErrorResponse, val code:Int?=0) : Resource<Nothing>()

//    class WrongClientTimeError(val response: ErrorResponse, val code:Int?=0) : Resource<Nothing>()
    class Loading(val loading :Boolean) : Resource<Nothing>()

}