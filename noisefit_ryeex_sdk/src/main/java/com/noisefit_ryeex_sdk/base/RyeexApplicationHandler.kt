package com.noisefit_ryeex_sdk.base

import android.content.Context
import com.noisefit_commans.interfaces.base.BaseInitializeCallbacks
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.interfaces.connection.ConnectionCallbacks
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.ConnectEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import com.ryeex.ble.connector.BleEngine
import com.ryeex.ble.connector.log.BleLogCallback
import com.ryeex.watch.adapter.device.WatchDevice
import java.util.*
import kotlin.concurrent.schedule
import javax.inject.Inject

private const val TAG = "RyeexApplicationHandler"

class RyeexApplicationHandler
@Inject
constructor(private val context: Context) : BaseInitializeInterface() {

    private var isInitSDK = false
    private var baseInitializeCallbacks: BaseInitializeCallbacks? = null
    private var watchDevice: WatchDevice? = null
    var connectionCallbacks: ConnectionCallbacks? = null

    fun getWatchDevice(): WatchDevice? {
        if (!isInitSDK) {
            initSdk()
        }
        if (watchDevice == null) {
            watchDevice = WatchDevice()
            return watchDevice
        }
        return watchDevice
    }

    override fun removeCallback() {
        baseInitializeCallbacks = null
    }

    fun setWatchDevice(bindingDevice: WatchDevice?) {
        if (bindingDevice == null) {
            watchDevice?.deviceRequestListener = null
            watchDevice?.removeDeviceConnectListener()
            watchDevice?.logout(null, null)
        }
        this.watchDevice = bindingDevice
    }


    override fun initSdk() {
        synchronized(this@RyeexApplicationHandler) {
            if (!isInitSDK) {
                BleEngine.init(context, true, object : BleLogCallback {
                    override fun verbose(p0: String?, p1: String?) {
                        LOGS.v("$TAG verbose: $p0 $p1")
                    }

                    override fun debug(p0: String?, p1: String?) {
                        LOGS.d(TAG, "debug: $p0 $p1")
                    }

                    override fun info(tag: String?, msg: String?) {
                        LOGS.i(TAG, "info: $tag $msg")
                        AppLogs.sendAppLogs("$tag: $msg")
                    }

                    override fun warn(p0: String?, p1: String?) {
                        LOGS.w(TAG, "warn: $p0 $p1")
                    }

                    override fun error(tag: String?, msg: String?) {
                        LOGS.e(TAG, "error: $msg $msg")
                        AppLogs.sendAppLogs("$tag: $msg")
                    }

                }, object : BleEngine.InitStatusCallback {
                    override fun onInitComplete() {
                        LOGS.i(TAG, "onInitComplete")
                        AppLogs.sendAppLogs("$TAG: onInitComplete")

                        //Don't remove this timer. callbackListener taking some time.
                        Timer("DelayConnection", false)
                            .schedule(500) {
                                isInitSDK = true
                                baseInitializeCallbacks?.serviceConnected()
                            }

                    }

                    override fun onInitError(code: Int) {

                        LOGS.e(TAG, "onInitError error: $code")
                        AppLogs.sendAppLogs("$TAG: onInitError error: $code")
                    }
                })
            }
        }
    }

    override fun unInitSdk() {
        synchronized(this@RyeexApplicationHandler) {
            setWatchDevice(null)
            BleEngine.release(object : BleEngine.ReleaseStatusCallback {
                override fun onReleaseComplete() {
                    LOGS.i(TAG, "onReleaseComplete")
                    AppLogs.sendAppLogs("$TAG: onReleaseComplete")
                    baseInitializeCallbacks?.serviceDisconnected()
                    isInitSDK = false
                    AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.Other.apply {
                        comment = "Release"
                    })
                }

                override fun onReleaseError(code: Int) {
                    LOGS.e(TAG, "onReleaseError error: $code")
                    AppLogs.sendAppLogs("$TAG: onReleaseError error: $code")
                }
            })
        }
    }


    override fun <T> callbackListener(callback: T) {
        baseInitializeCallbacks = callback as BaseInitializeCallbacks
    }

}