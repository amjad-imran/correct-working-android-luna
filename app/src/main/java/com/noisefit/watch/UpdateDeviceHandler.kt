package com.noisefit.watch

import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.AppLogs
import javax.inject.Inject

class UpdateDeviceHandler
@Inject
constructor(
    private val zhUpdateDeviceAction: UpdateDeviceDataActions,
    private val watchesSdk: WatchesSDK
) {


    fun getQueryActions(connectedDevice: ColorFitDevice): UpdateDeviceDataActions? {
        when (watchesSdk.getWatchType(connectedDevice)) {


            SDKWatchType.SDK_ZH->{
                zhUpdateDeviceAction.init()
                zhUpdateDeviceAction.attachCallbacks()
                CommonGlobals.updateDeviceDataActions = zhUpdateDeviceAction
                AppLogs.sendAppLogs("UpdateDevice Actions for ZH SDK")
            }


        }
        return CommonGlobals.updateDeviceDataActions
    }
}