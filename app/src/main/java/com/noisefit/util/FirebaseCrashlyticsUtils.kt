package com.noisefit.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject

class FirebaseCrashlyticsUtils
@Inject
constructor(var localDataStore: DataStoredInterface) {

    fun setCrashlyticsUserProperty() {
        LOGS.d("setting setCrashlyticsUserProperty")
        localDataStore.getUser()?.let { user ->
            user.id?.let { userId ->
                FirebaseCrashlytics.getInstance().setUserId(userId.toString())
            }
            user.email?.let { email ->
                FirebaseCrashlytics.getInstance().setCustomKey("email", email)
            }
            user.firstName?.let { name ->
                FirebaseCrashlytics.getInstance().setCustomKey("name", name)
            }
            user.mobile?.let { mobile ->
                FirebaseCrashlytics.getInstance().setCustomKey("mobile", mobile)
            }
        }

        localDataStore.getConnectedDevice()?.let { colorFitDevice ->
            colorFitDevice.deviceType?.let { deviceType ->
                FirebaseCrashlytics.getInstance().setCustomKey("device", deviceType)
            }
        }
    }
}