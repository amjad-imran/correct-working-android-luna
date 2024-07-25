package com.oreo.ui.sleep2.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OSPTrendsSharedViewModel @Inject constructor() : BaseViewModel() {
    fun sendInteractPos(position: Int) {
        _interactGraphData.postValue(position)
    }

    private val _interactGraphData =
        MutableLiveData<Int>()
    val interactGraphData: LiveData<Int> = _interactGraphData


}