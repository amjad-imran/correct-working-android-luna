package com.noisefit_commans.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.utils.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BaseViewModelCompose : ViewModel() {
    private val _message = MutableLiveData<Event<String>>()
    private val _loading = MutableStateFlow<Boolean>(false)
    private val _apiError = MutableLiveData<Event<ErrorResponse>>()

    fun getMessages(): LiveData<Event<String>> = _message
    fun getLoading(): StateFlow<Boolean> = _loading
    fun getApiErrors(): LiveData<Event<ErrorResponse>> = _apiError

    fun sendMessage(string: String?) {
        _message.postValue(Event(string))
    }


    fun setApiErrors(response: ErrorResponse) {
        _apiError.postValue(Event(response))
    }

    fun setLoading(loading: Boolean) {
        _loading.value = (loading)
    }
}