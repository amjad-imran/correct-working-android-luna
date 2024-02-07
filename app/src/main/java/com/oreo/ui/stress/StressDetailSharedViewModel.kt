package com.oreo.ui.stress

import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.data.enums.StressType
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StressDetailSharedViewModel @Inject
constructor(
) : BaseViewModel() {

    val selectedStressLevel = MutableLiveData<StressType>()

    fun setSelectedType(type: StressType) {
        selectedStressLevel.value = type
    }

    var lastSelectedStressType: StressType = StressType.CALM


}