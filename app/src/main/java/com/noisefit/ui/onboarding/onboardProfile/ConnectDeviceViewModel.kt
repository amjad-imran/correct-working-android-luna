package com.noisefit.ui.onboarding.onboardProfile

import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConnectDeviceViewModel @Inject constructor(
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore
) : BaseViewModel() {


    fun isRingPaired(): Boolean {
        return ringDataStore.getRingDevice() != null
    }
}