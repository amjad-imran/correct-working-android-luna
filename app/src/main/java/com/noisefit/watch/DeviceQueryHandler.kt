package com.noisefit.watch

import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.AppLogs
import javax.inject.Inject

class DeviceQueryHandler
@Inject
constructor(
    private val zhQueryAction: QueryDeviceDataActions,
    private val watchesSdk: WatchesSDK
) {


    fun getQueryActions(connectedDevice: ColorFitDevice): QueryDeviceDataActions? {
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
                zhQueryAction.init()
                zhQueryAction.attachCallbacks()
                CommonGlobals.queryDeviceDataActions = zhQueryAction
                AppLogs.sendAppLogs("DeviceQuery Actions for ZH SDK")
            }
            SDKWatchType.SDK_RYEEX->{

            }

        }
        return CommonGlobals.queryDeviceDataActions
    }
}