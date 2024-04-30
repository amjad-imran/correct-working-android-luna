package com.oreo.ui.stress

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.enums.StressType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StressDetailSharedViewModel @Inject
constructor(
    val localDataStore: DataStoredInterface
) : BaseViewModel() {

    val selectedStressLevel = MutableLiveData<StressType>()

    fun setSelectedType(type: StressType) {
        val lastValue = selectedStressLevel.value
        if (lastValue != type) {
            selectedStressLevel.value = type
        }
    }



    var lastSelectedStressType: StressType = StressType.CALM


}