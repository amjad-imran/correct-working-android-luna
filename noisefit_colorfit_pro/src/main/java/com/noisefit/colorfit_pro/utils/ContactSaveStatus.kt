package com.noisefit.colorfit_pro.utils


sealed class ContactSaveStatus<out T> {

    data class Success<out T>(val value: T) : ContactSaveStatus<T>()
    data class GenericError(
        val errorMessage: String? = null
    ) : ContactSaveStatus<Nothing>()
}