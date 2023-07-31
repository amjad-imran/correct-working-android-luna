package com.noisefit.watch

import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.ConnectEvents
import com.noisefit_commans.utils.LogEvents
import javax.inject.Inject

class UserActivityHandler
@Inject

constructor(
    private val zhUserActivityDataActions: UserActivityDataActions,
    private val watchesSdk: WatchesSDK
) {


    fun getUserActivityActions(connectedDevice: ColorFitDevice): UserActivityDataActions? {
        when (watchesSdk.getWatchType(connectedDevice)) {
            SDKWatchType.SDK_CF_PRO -> {


            }
            SDKWatchType.SDK_QUBE -> {

            }
            SDKWatchType.SDK_EVOLVE -> {


            }
            SDKWatchType.SDK_HYBRID -> {


            }
            SDKWatchType.SDK_NAV_PLUS -> {


            }
            SDKWatchType.SDK_ZH->{
                zhUserActivityDataActions.init()
                zhUserActivityDataActions.attachCallbacks()
                CommonGlobals.userActivityDataActions = zhUserActivityDataActions
                AppLogs.sendAppLogs("ZH Sdk watch type")

            }
            SDKWatchType.SDK_RYEEX->{


            }

        }
        return CommonGlobals.userActivityDataActions
    }
}