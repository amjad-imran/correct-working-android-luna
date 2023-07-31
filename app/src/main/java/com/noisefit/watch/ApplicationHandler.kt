package com.noisefit.watch

import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject


class ApplicationHandler
@Inject
constructor(
    private var zhApplicationHandler: BaseInitializeInterface,
    private val watchesSdk: WatchesSDK
) {


    private var noisefitZhSdkInitiated = false

    fun initSdks(connectedDevice: ColorFitDevice?): BaseInitializeInterface? {
        var baseInitializeInterface: BaseInitializeInterface? = null
        LOGS.d("Init sdk ${connectedDevice.toString()}")
        connectedDevice?.let {
            when (watchesSdk.getWatchType(it)) {
                SDKWatchType.SDK_ZH->{
                    initiateNoisefitZhSdk()
                    baseInitializeInterface = zhApplicationHandler
                    AppLogs.sendAppLogs("Init ZH SDK")
                }

                SDKWatchType.SDK_NAV_PLUS -> TODO()
                SDKWatchType.SDK_HYBRID -> TODO()
                SDKWatchType.SDK_QUBE -> TODO()
                SDKWatchType.SDK_EVOLVE -> TODO()
                SDKWatchType.SDK_CF_PRO -> TODO()
                SDKWatchType.SDK_RYEEX -> TODO()
            }
        }
        return baseInitializeInterface
    }

    fun unInitSdks(connectedDevice: ColorFitDevice?) {
        connectedDevice?.let {
            when (watchesSdk.getWatchType(it)) {
                SDKWatchType.SDK_ZH->{
                    zhApplicationHandler.unInitSdk()
                    noisefitZhSdkInitiated = false
                    AppLogs.sendAppLogs("Un init ZH SDK")
                }

                SDKWatchType.SDK_NAV_PLUS -> TODO()
                SDKWatchType.SDK_HYBRID -> TODO()
                SDKWatchType.SDK_QUBE -> TODO()
                SDKWatchType.SDK_EVOLVE -> TODO()
                SDKWatchType.SDK_CF_PRO -> TODO()
                SDKWatchType.SDK_RYEEX -> TODO()
            }
        }
    }




    fun initiateNoisefitZhSdk() {
        if (noisefitZhSdkInitiated.not()) {
            zhApplicationHandler.initSdk()
            noisefitZhSdkInitiated = true
        }
    }

    fun removeCallback(){

    }

}