package com.noisefit.util

import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import javax.inject.Inject

class TestModeUtils
@Inject
constructor(
    private val watchesSDK: WatchesSDK,
    private var localDataStore: DataStoredInterface
) {

    fun saveHrHash(): Boolean {
        if (watchesSDK.getWatchType() == SDKWatchType.SDK_QUBE && localDataStore.getIsTestModeOn()) {
            return false
        }
        return true
    }
}