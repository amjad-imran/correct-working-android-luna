package com.noisefit.watch

import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject


class ApplicationHandler
@Inject
constructor(
    private var nfhApplicationHandler: BaseInitializeInterface,
    private var cf2ApplicationHandler: BaseInitializeInterface,
    private var navPlusApplicationHandler: BaseInitializeInterface,
    private var zhApplicationHandler: BaseInitializeInterface,
    private var proApplicationHandler: BaseInitializeInterface,
    private var evolveApplicationHandler: BaseInitializeInterface,
    private var ryeexApplicationHandler: BaseInitializeInterface,
    private val watchesSdk: WatchesSDK
) {


    private var colorfit2SdkInitiated = false
    private var colorfitProSdkInitiated = false
    private var noisefitEvolveSdkInitiated = false
    private var noisefitHybridSdkInitiated = false
    private var noisefitNavPlusSdkInitiated = false
    private var noisefitZhSdkInitiated = false
    private var noisefitRyeexSdkInitiated = false

    fun initSdks(connectedDevice: ColorFitDevice?): BaseInitializeInterface? {
        var baseInitializeInterface: BaseInitializeInterface? = null
        LOGS.d("Init sdk ${connectedDevice.toString()}")
        connectedDevice?.let {
            when (watchesSdk.getWatchType(it)) {

                SDKWatchType.SDK_CF_PRO -> {

                    initiateColorfit2Sdk()
                    baseInitializeInterface = cf2ApplicationHandler
                    AppLogs.sendAppLogs("Init Colorfit Pro SDK")
                }
                SDKWatchType.SDK_QUBE -> {
                    baseInitializeInterface = proApplicationHandler
                    initiateColorfitProSdk()
                    AppLogs.sendAppLogs("Init Qube SDK")
                }
                SDKWatchType.SDK_EVOLVE -> {
                    baseInitializeInterface = evolveApplicationHandler
                    initiateNoisefitEvolveSdk()
                    AppLogs.sendAppLogs("Init Evolve SDK")
                }
                SDKWatchType.SDK_HYBRID  -> {
                    initiateNoisefitHybridSdk()
                    baseInitializeInterface = nfhApplicationHandler
                    AppLogs.sendAppLogs("Init Hybrid SDK")
                }
                SDKWatchType.SDK_NAV_PLUS -> {
                    initiateNoisefitNavPlusSdk()
                    baseInitializeInterface = navPlusApplicationHandler
                    AppLogs.sendAppLogs("Init Nav Plus SDK")
                }
                SDKWatchType.SDK_ZH->{
                    initiateNoisefitZhSdk()
                    baseInitializeInterface = zhApplicationHandler
                    AppLogs.sendAppLogs("Init ZH SDK")
                }
                SDKWatchType.SDK_RYEEX->{
                    initiateNoisefitRyeexSdk()
                    baseInitializeInterface = ryeexApplicationHandler
                    AppLogs.sendAppLogs("Init RYEEX SDK")
                }
            }
        }
        return baseInitializeInterface
    }

    fun unInitSdks(connectedDevice: ColorFitDevice?) {
        connectedDevice?.let {
            when (watchesSdk.getWatchType(it)) {
                SDKWatchType.SDK_CF_PRO -> {
                    cf2ApplicationHandler.unInitSdk()
                    colorfit2SdkInitiated = false
                    AppLogs.sendAppLogs("Un init Colorfit Pro SDK")
                }
                SDKWatchType.SDK_QUBE -> {
                    proApplicationHandler.unInitSdk()
                    colorfitProSdkInitiated = false
                    AppLogs.sendAppLogs("Un init Qube SDK")
                }
                SDKWatchType.SDK_EVOLVE  -> {
                    evolveApplicationHandler.unInitSdk()
                    noisefitEvolveSdkInitiated = false
                    AppLogs.sendAppLogs("Un init Evolve SDK")
                }
                SDKWatchType.SDK_HYBRID  -> {
                    nfhApplicationHandler.unInitSdk()
                    noisefitHybridSdkInitiated = false
                    AppLogs.sendAppLogs("Un init Hybrid SDK")
                }
                SDKWatchType.SDK_NAV_PLUS -> {
                    navPlusApplicationHandler.unInitSdk()
                    noisefitNavPlusSdkInitiated = false
                    AppLogs.sendAppLogs("Un init NAV Plus SDK")
                }
                SDKWatchType.SDK_ZH->{
                    zhApplicationHandler.unInitSdk()
                    noisefitZhSdkInitiated = false
                    AppLogs.sendAppLogs("Un init ZH SDK")
                }
                SDKWatchType.SDK_RYEEX->{
                    ryeexApplicationHandler.unInitSdk()
                    noisefitRyeexSdkInitiated = false
                    AppLogs.sendAppLogs("Un init RYEEX SDK")
                }
            }
        }
    }

    fun initSdks() {
        try {
            initiateNoisefitHybridSdk()
        } catch (e: Exception) {
        }
        try {
            initiateColorfit2Sdk()
        } catch (e: Exception) {
        }
        try {
            initiateColorfitProSdk()
        } catch (e: Exception) {
        }
        try {
            initiateNoisefitEvolveSdk()
        } catch (e: Exception) {
        }
        try {
            initiateNoisefitNavPlusSdk()
        } catch (e: Exception) {
        }
        try {
            initiateNoisefitRyeexSdk()
        } catch (e: Exception) {
        }

    }


    private fun initiateColorfit2Sdk() {
        if (colorfit2SdkInitiated.not()) {
            cf2ApplicationHandler.initSdk()
            colorfit2SdkInitiated = true
        }
    }

    private fun initiateColorfitProSdk() {
        if (colorfitProSdkInitiated.not()) {
            proApplicationHandler.initSdk()
            colorfitProSdkInitiated = true
        }
    }

    //  pro
//ultra
    private fun initiateNoisefitEvolveSdk() {
        if (noisefitEvolveSdkInitiated.not()) {
            evolveApplicationHandler.initSdk()
            noisefitEvolveSdkInitiated = true
        }
    }

    fun initiateNoisefitHybridSdk() {
        if (noisefitHybridSdkInitiated.not()) {
            nfhApplicationHandler.initSdk()
            noisefitHybridSdkInitiated = true
        }
    }

    fun initiateNoisefitNavPlusSdk() {
        if (noisefitNavPlusSdkInitiated.not()) {
            navPlusApplicationHandler.initSdk()
            noisefitNavPlusSdkInitiated = true
        }
    }
    fun initiateNoisefitZhSdk() {
        if (noisefitZhSdkInitiated.not()) {
            zhApplicationHandler.initSdk()
            noisefitZhSdkInitiated = true
        }
    }
    fun initiateNoisefitRyeexSdk() {
        if (noisefitRyeexSdkInitiated.not()) {
            ryeexApplicationHandler.initSdk()
            noisefitRyeexSdkInitiated = true
        }
    }
    fun removeCallback(){

    }

}