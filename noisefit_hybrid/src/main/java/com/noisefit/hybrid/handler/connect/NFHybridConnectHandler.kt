package com.noisefit.hybrid.handler.connect

import android.os.Handler
import android.os.Looper
import cn.appscomm.bluetoothsdk.app.BluetoothSDK
import cn.appscomm.bluetoothsdk.interfaces.ResultCallBack
import com.noisefit.hybrid.base.NFHybridApplicationHandler
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.connection.ConnectionCallbacks
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.ConnectEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import javax.inject.Inject

class NFHybridConnectHandler
@Inject constructor(
    private val applicationHandler: NFHybridApplicationHandler
) : ConnectionDataActions() {
    private var baseConnectionCallbacks: ConnectionCallbacks? = null
    private var noiseFitDevice: ColorFitDevice? = null
    private var isReconnect = false

    private val TAG = "NFHybridConnectHandler"

    companion object {
        private var qrCodeBind = false
    }

    override fun <T> callbackListener(callback: T) {
        baseConnectionCallbacks = callback as ConnectionCallbacks
    }

    override fun <T> callbackListenerNew(callback: T) {}

    override fun init() {
        super.init()
        AppLogs.sendAppLogs("SDK initialized")
        try {
            applicationHandler.initSdk()
        } catch (e: Exception) {
            AppLogs.sendAppLogs("SDK Initialization failed")
            e.printStackTrace()
        }
    }

    override fun connect(noiseFitDevice: ColorFitDevice) {
        AppLogs.sendAppLogs("ConnectDevice : " + noiseFitDevice.address)
        baseConnectionCallbacks?.onConnect(ConnectState.Connecting(noiseFitDevice))
        this.noiseFitDevice = noiseFitDevice
        if (isConnected()) {
            connectSuccess()
        } else {
//            disconnect(noiseFitDevice)
            Handler(Looper.getMainLooper()).postDelayed({
                val address = noiseFitDevice.address
                AppLogs.sendAppLogs("$TAG Inside connect - Not connected make a connection $noiseFitDevice")
                if(applicationHandler.sdkInitStatus){
                    BluetoothSDK.connectByMAC(resultCallBack, address)
                }else{
                    AppLogs.sendAppLogs("$TAG Inside connect - SDK init false, re initializing")
                    init()
                }

            }, 5000)
        }
    }

    override fun checkWatchBindStatus() {
        BluetoothSDK.checkInit(resultCallBack)
    }

    override fun onConnectedQRBinding() {
        qrCodeBind = true
    }

    private fun connectSuccess() {
        AppLogs.sendAppLogs("Connect Success")
        if (isReconnect) {
            BluetoothSDK.checkInit(resultCallBack)
            isReconnect = false
        } else {
            noiseFitDevice?.let {
                baseConnectionCallbacks?.onConnect(ConnectState.ConnectSuccess(it))
                baseConnectionCallbacks?.onDeviceReady(it)
            }
        }

    }


    private val resultCallBack: ResultCallBack = object : ResultCallBack {
        override fun onSuccess(resultType: Int, objects: Array<Any>?) {
            LOGS.d("$TAG Success $resultType")

            if (resultType == ResultCallBack.TYPE_CONNECT) {
                //Log.e("noise_fit_event:rn_command", "helllllll  in success");
                if (qrCodeBind) {
                    onQRCodeBind()
                    qrCodeBind = false
                }
                if (!isReconnect) {
                    BluetoothSDK.bindStart(this)
                }
                BluetoothSDK.setIsAutoReconnect(false)
                connectSuccess()

            } else if (resultType == ResultCallBack.TYPE_DISCONNECT) {
                BluetoothSDK.bindEnd(this)
                AppLogs.sendAppLogs("Disconnect Success")
                disconnectSuccess()
            } else if (resultType == ResultCallBack.TYPE_BIND_START) {
                BluetoothSDK.bindEnd(this)

            } else if (resultType == ResultCallBack.TYPE_BIND_END) {

            } else if (resultType == ResultCallBack.TYPE_RESTORE_FACTORY) {
                BluetoothSDK.disConnect(this)
            } else if (resultType == ResultCallBack.TYPE_CHECK_INIT) {
                objects?.let {
                    if (objects[0] == true) {
                        connectSuccess()
                    } else {
                        restoreDevice()
                    }
                }
            }
        }

        override fun onFail(resultType: Int) {
            isReconnect = false
            AppLogs.sendAppLogs(LogEvents.Connect,ConnectEvents.Failed.apply { comment= "$TAG Failed $resultType"})
            if (resultType == ResultCallBack.TYPE_CONNECT) {
                baseConnectionCallbacks?.onConnect(ConnectState.ConnectFailed(noiseFitDevice))
            } else if (resultType == ResultCallBack.TYPE_DISCONNECT) {
                baseConnectionCallbacks?.onConnect(ConnectState.DisconnectFailed(noiseFitDevice))
            }
        }
    }

    private fun disconnectSuccess() {
        isReconnect = false
        baseConnectionCallbacks?.onConnect(ConnectState.DisconnectSuccess(noiseFitDevice))
    }

    override fun disconnect(noiseFitDevice: ColorFitDevice) {

        restoreDevice()
    }

    private fun restoreDevice() {
        AppLogs.sendAppLogs("Restore Device")
        BluetoothSDK.restoreFactory(resultCallBack)
    }

    override fun reconnect(noiseFitDevice: ColorFitDevice, type: Boolean) {
        isReconnect = true

        val isConnected = isConnected()
        AppLogs.sendAppLogs("$TAG reconnect: $isConnected")
        if (!isConnected) {
            connect(noiseFitDevice)
        } else {
            connectSuccess()
        }
    }

    override fun isDevicePaired(noiseFitDevice: ColorFitDevice): Boolean {
        return true
    }

    override fun isConnected(): Boolean {
        return try {
            if (applicationHandler.sdkInitStatus) {
                BluetoothSDK.isConnected()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun onQRCodeBind() {
        AppLogs.sendAppLogs("onQRCodeBind start")
        BluetoothSDK.bindStartQRCode(object : ResultCallBack {
            override fun onSuccess(i: Int, objects: Array<Any>?) {
                AppLogs.sendAppLogs("QRCodeBind onSuccess")
                BluetoothSDK.bindEnd(object : ResultCallBack {
                    override fun onSuccess(i: Int, objects: Array<Any>?) {
                        AppLogs.sendAppLogs("QR bindEnd  Success")
                    }

                    override fun onFail(i: Int) {
                        AppLogs.sendAppLogs("onQRCodeBind fail")
                        Handler(Looper.getMainLooper()).postDelayed({
                            noiseFitDevice?.let { connect(it) }
                        }, 2000)
                    }
                })
            }

            override fun onFail(i: Int) {
                AppLogs.sendAppLogs(
                    LogEvents.Connect,
                    ConnectEvents.Failed.apply { comment = "onQRCodeBind fail : $i" })
                Handler(Looper.getMainLooper()).postDelayed({
                    noiseFitDevice?.let {
                        connect(it)
                    }
                }, 2000)
            }
        })
    }


}