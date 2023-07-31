package com.noisefit.watch

import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.AppLogs
import javax.inject.Inject

class DeviceQueryHandler
@Inject
constructor(
    private val nfhQueryAction: QueryDeviceDataActions,
    private val cf2QueryAction: QueryDeviceDataActions,
    private val navPlusQueryAction: QueryDeviceDataActions,
    private val zhQueryAction: QueryDeviceDataActions,
    private val proQueryAction: QueryDeviceDataActions,
    private val evolveQueryAction: QueryDeviceDataActions,
    private val ryeexQueryAction: QueryDeviceDataActions,
    private val watchesSdk: WatchesSDK
) {


    fun getQueryActions(connectedDevice: ColorFitDevice): QueryDeviceDataActions? {
        when (watchesSdk.getWatchType(connectedDevice)) {
            SDKWatchType.SDK_CF_PRO -> {
                cf2QueryAction.init()
                cf2QueryAction.attachCallbacks()
                CommonGlobals.queryDeviceDataActions = cf2QueryAction
                AppLogs.sendAppLogs("DeviceQuery Actions for Colorfit Pro SDK")
            }
            SDKWatchType.SDK_QUBE -> {
                proQueryAction.init()
                proQueryAction.attachCallbacks()
                CommonGlobals.queryDeviceDataActions = proQueryAction
                AppLogs.sendAppLogs("DeviceQuery Actions for Qube SDK")
            }
            SDKWatchType.SDK_EVOLVE -> {
                evolveQueryAction.init()
                evolveQueryAction.attachCallbacks()
                CommonGlobals.queryDeviceDataActions = evolveQueryAction
                AppLogs.sendAppLogs("DeviceQuery Actions for Evolve SDK")
            }
            SDKWatchType.SDK_HYBRID -> {
                nfhQueryAction.init()
                nfhQueryAction.attachCallbacks()
                CommonGlobals.queryDeviceDataActions = nfhQueryAction
                AppLogs.sendAppLogs("DeviceQuery Actions for Hybrid SDK")
            }
            SDKWatchType.SDK_NAV_PLUS -> {
                navPlusQueryAction.init()
                navPlusQueryAction.attachCallbacks()
                CommonGlobals.queryDeviceDataActions = navPlusQueryAction
                AppLogs.sendAppLogs("DeviceQuery Actions for Nav Plus SDK")
            }
            SDKWatchType.SDK_ZH->{
                zhQueryAction.init()
                zhQueryAction.attachCallbacks()
                CommonGlobals.queryDeviceDataActions = zhQueryAction
                AppLogs.sendAppLogs("DeviceQuery Actions for ZH SDK")
            }
            SDKWatchType.SDK_RYEEX->{
                ryeexQueryAction.init()
                ryeexQueryAction.attachCallbacks()
                CommonGlobals.queryDeviceDataActions = ryeexQueryAction
                AppLogs.sendAppLogs("DeviceQuery Actions for ZH SDK")
            }

        }
        return CommonGlobals.queryDeviceDataActions
    }
}