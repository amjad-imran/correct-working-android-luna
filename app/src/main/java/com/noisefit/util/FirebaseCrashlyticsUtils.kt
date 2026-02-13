package com.noisefit.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject

class FirebaseCrashlyticsUtils
@Inject
constructor(var localDataStore: DataStoredInterface, var ringDataStore: RingDataStore) {

    fun setCrashlyticsUserProperty() {
        LOGS.d("setting setCrashlyticsUserProperty")
        localDataStore.getUser()?.let { user ->
            user.id?.let { userId ->
                FirebaseCrashlytics.getInstance().setUserId(userId.toString())
            }
        }

        ringDataStore.getRingDevice()?.let { colorFitDevice ->
            colorFitDevice.deviceType?.let { deviceType ->
                FirebaseCrashlytics.getInstance().setCustomKey("device", deviceType)
            }
        }
    }
}