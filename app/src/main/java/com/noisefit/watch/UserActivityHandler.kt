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
    private val nfhUserActivityDataActions: UserActivityDataActions,
    private val navUserActivityDataActions: UserActivityDataActions,
    private val zhUserActivityDataActions: UserActivityDataActions,
    private val cf2UserActivityDataActions: UserActivityDataActions,
    private val proUserActivityHandler: UserActivityDataActions,
    private val evolveUserActivityHandler: UserActivityDataActions,
    private val ryeexUserActivityHandler: UserActivityDataActions,
    private val watchesSdk: WatchesSDK
) {


    fun getUserActivityActions(connectedDevice: ColorFitDevice): UserActivityDataActions? {
        when (watchesSdk.getWatchType(connectedDevice)) {
            SDKWatchType.SDK_CF_PRO -> {
                cf2UserActivityDataActions.init()
                cf2UserActivityDataActions.attachCallbacks()
                CommonGlobals.userActivityDataActions = cf2UserActivityDataActions
                AppLogs.sendAppLogs("Color fit Pro Sdk watch type")

            }
            SDKWatchType.SDK_QUBE -> {
                proUserActivityHandler.init()
                proUserActivityHandler.attachCallbacks()
                CommonGlobals.userActivityDataActions = proUserActivityHandler
                AppLogs.sendAppLogs("Qube Sdk watch type")

            }
            SDKWatchType.SDK_EVOLVE -> {
                evolveUserActivityHandler.init()
                evolveUserActivityHandler.attachCallbacks()
                CommonGlobals.userActivityDataActions = evolveUserActivityHandler
                AppLogs.sendAppLogs("Evolve Sdk watch type")

            }
            SDKWatchType.SDK_HYBRID -> {
                nfhUserActivityDataActions.init()
                nfhUserActivityDataActions.attachCallbacks()
                CommonGlobals.userActivityDataActions = nfhUserActivityDataActions
                AppLogs.sendAppLogs("Hybrid Sdk watch type")

            }
            SDKWatchType.SDK_NAV_PLUS -> {
                navUserActivityDataActions.init()
                navUserActivityDataActions.attachCallbacks()
                CommonGlobals.userActivityDataActions = navUserActivityDataActions
                AppLogs.sendAppLogs("NAV Plus Sdk watch type")

            }
            SDKWatchType.SDK_ZH->{
                zhUserActivityDataActions.init()
                zhUserActivityDataActions.attachCallbacks()
                CommonGlobals.userActivityDataActions = zhUserActivityDataActions
                AppLogs.sendAppLogs("ZH Sdk watch type")

            }
            SDKWatchType.SDK_RYEEX->{
                ryeexUserActivityHandler.init()
                ryeexUserActivityHandler.attachCallbacks()
                CommonGlobals.userActivityDataActions = ryeexUserActivityHandler
                AppLogs.sendAppLogs("ZH Sdk watch type")

            }

        }
        return CommonGlobals.userActivityDataActions
    }
}