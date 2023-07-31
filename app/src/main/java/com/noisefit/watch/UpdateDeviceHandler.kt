package com.noisefit.watch

import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.AppLogs
import javax.inject.Inject

class UpdateDeviceHandler
@Inject
constructor(
    private val nfhUpdateDeviceAction: UpdateDeviceDataActions,
    private val cf2UpdateDeviceAction: UpdateDeviceDataActions,
    private val navPlusUpdateDeviceAction: UpdateDeviceDataActions,
    private val zhUpdateDeviceAction: UpdateDeviceDataActions,
    private val proUpdateDeviceAction: UpdateDeviceDataActions,
    private val evolveUpdateDeviceAction: UpdateDeviceDataActions,
    private val ryeexUpdateDeviceAction: UpdateDeviceDataActions,
    private val watchesSdk: WatchesSDK
) {


    fun getQueryActions(connectedDevice: ColorFitDevice): UpdateDeviceDataActions? {
        when (watchesSdk.getWatchType(connectedDevice)) {
            SDKWatchType.SDK_CF_PRO -> {
                cf2UpdateDeviceAction.init()
                cf2UpdateDeviceAction.attachCallbacks()
                CommonGlobals.updateDeviceDataActions = cf2UpdateDeviceAction
                AppLogs.sendAppLogs("UpdateDevice Actions for Colorfit Pro SDK")

            }
            SDKWatchType.SDK_QUBE -> {
                proUpdateDeviceAction.init()
                proUpdateDeviceAction.attachCallbacks()
                CommonGlobals.updateDeviceDataActions = proUpdateDeviceAction
                AppLogs.sendAppLogs("UpdateDevice Actions for Qube SDK")
            }
            SDKWatchType.SDK_EVOLVE -> {
                evolveUpdateDeviceAction.init()
                evolveUpdateDeviceAction.attachCallbacks()
                CommonGlobals.updateDeviceDataActions = evolveUpdateDeviceAction
                AppLogs.sendAppLogs("UpdateDevice Actions for Evolve SDK")
            }
            SDKWatchType.SDK_HYBRID -> {
                nfhUpdateDeviceAction.init()
                nfhUpdateDeviceAction.attachCallbacks()
                CommonGlobals.updateDeviceDataActions = nfhUpdateDeviceAction
                AppLogs.sendAppLogs("UpdateDevice Actions for Hybrid SDK")
            }
            SDKWatchType.SDK_NAV_PLUS -> {
                navPlusUpdateDeviceAction.init()
                navPlusUpdateDeviceAction.attachCallbacks()
                CommonGlobals.updateDeviceDataActions = navPlusUpdateDeviceAction
                AppLogs.sendAppLogs("UpdateDevice Actions for NAV Plus SDK")
            }
            SDKWatchType.SDK_ZH->{
                zhUpdateDeviceAction.init()
                zhUpdateDeviceAction.attachCallbacks()
                CommonGlobals.updateDeviceDataActions = zhUpdateDeviceAction
                AppLogs.sendAppLogs("UpdateDevice Actions for ZH SDK")
            }
            SDKWatchType.SDK_RYEEX->{
                ryeexUpdateDeviceAction.init()
                ryeexUpdateDeviceAction.attachCallbacks()
                CommonGlobals.updateDeviceDataActions = ryeexUpdateDeviceAction
                AppLogs.sendAppLogs("UpdateDevice Actions for RYEEX SDK")
            }

        }
        return CommonGlobals.updateDeviceDataActions
    }
}