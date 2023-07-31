package com.noisefit_commans.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.utils.Event

open class BaseViewModel : ViewModel() {
    private val _message = MutableLiveData<Event<String>>()
    private val _loading = MutableLiveData<Boolean>()
    private val _apiError = MutableLiveData<Event<ErrorResponse>>()
    private var _dispatchRetry: (() -> Unit)? = null

    fun getMessages(): LiveData<Event<String>> = _message
    fun getLoading(): LiveData<Boolean> = _loading
    fun getApiErrors(): LiveData<Event<ErrorResponse>> = _apiError

    fun sendMessage(string: String?) {
        _message.postValue(Event(string))
    }

    fun setRetry(call: (()-> Unit)?) {
        _dispatchRetry = call
    }
    fun setApiErrors(response: ErrorResponse) {
        _apiError.postValue(Event(response))
    }

    fun setLoading(loading: Boolean) {
        _loading.postValue(loading)
    }
}