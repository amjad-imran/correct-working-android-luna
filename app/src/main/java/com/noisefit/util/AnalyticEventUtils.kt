package com.noisefit.util

import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import javax.inject.Inject


class AnalyticEventUtils
@Inject
constructor(
    var localDataStore: DataStoredInterface
) {

    companion object {


        const val CloudWatchFace = "Cloud Watchface"
        const val CustomWatchFace = "Custom Watchface"
        const val InbuiltWatchFace = "Inbuilt Watchface"


    }

    fun getEventProperty(
        type: String? = null,
        watchFaceUrl: String? = null
    ): HashMap<String, Any?> {

        var userId = "not logged in"
        var name = ""
        var device = ""
        localDataStore.getUser()?.let { user ->
            user.id?.let { uId ->
                userId = uId.toString()
            }

            user.firstName?.let { it ->
                name = it
            }
        }
        /*localDataStore.getConnectedDevice()?.let { colorFitDevice ->
            colorFitDevice.deviceType?.let { deviceType ->
                device = deviceType
            }
        }*/

        return java.util.HashMap<String, Any?>().apply {
            this["user_id"] = userId
            this["watch_name"] = device
            this["platform"] = "android"
            type?.let {
                this["type"] = type
            }

        }
    }

}