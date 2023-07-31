package com.noisefit_zhsdk.base

import android.os.Handler
import android.os.Looper
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.interfaces.base.BaseInitializeCallbacks
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.ConnectEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.DeviceLogCallBack
import com.zhapp.ble.callback.ZHInitStatusCallBack
import javax.inject.Inject

class ZhApplicationHandler
@Inject
constructor() : BaseInitializeInterface() {

    private var baseInitializeCallbacks: BaseInitializeCallbacks? = null
    private var controlBleTools: ControlBleTools? = null
    private val TAG = "ZhApplicationHandler"
    var isInitSDK = false
    override fun <T> callbackListener(callback: T) {
        baseInitializeCallbacks = callback as BaseInitializeCallbacks
    }

    override fun removeCallback() {
        baseInitializeCallbacks = null
    }

    fun getZhService(): ControlBleTools? {
        if (controlBleTools == null) {
            controlBleTools = ControlBleTools.getInstance()
        } else {
            controlBleTools
        }
        return controlBleTools
    }

    override fun initSdk() {
        synchronized(this@ZhApplicationHandler) {
            LOGS.i(TAG, "initSdk:" + isInitSDK)
            if (!isInitSDK) {
                ControlBleTools.getInstance().setInitStatusCallBack(object : ZHInitStatusCallBack {
                    override fun onInitComplete() {
                        isInitSDK = true
                        LOGS.i(TAG, "initSdk: onInitComplete()")

                        controlBleTools = ControlBleTools.getInstance()
                        baseInitializeCallbacks?.serviceConnected()
                        AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.Other.apply {
                            comment = "Connected"
                        })
                    }

                })
                ControlBleTools.getInstance().init(NoisefitApplication.context)
                AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.Other.apply {
                    comment = "Initialized"
                })
            }
        }
    }


    override fun unInitSdk() {
        synchronized(this@ZhApplicationHandler) {
            LOGS.i(TAG, "uninitSdk:" + isInitSDK)
            ControlBleTools.getInstance().release()
            isInitSDK = false
            AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.Other.apply {
                comment = "Release"
            })
        }
    }
}