package com.noisefit.ui.dashboard.graphs.sleep

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.data.enums.SleepExtraType
import com.noisefit_commans.ui.BaseViewModel

class SleepSharedViewModel : BaseViewModel() {


    private val _isSelected = MutableLiveData<SleepExtraType>()
    val isSelected: LiveData<SleepExtraType> = _isSelected


    fun setSelected(isSelected: SleepExtraType) {
        _isSelected.value = isSelected
    }
}