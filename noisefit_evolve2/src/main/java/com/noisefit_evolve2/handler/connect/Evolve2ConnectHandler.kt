package com.noisefit_evolve2.handler.connect

import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Looper
import com.noisefit_commans.constants.ConnectionEventsConstants
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.connection.ConnectionCallbacks
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceFirmware
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.ConnectEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import com.noisefit_evolve2.base.Evolve2ApplicationHandler
import com.touchgui.sdk.TGCallback
import com.touchgui.sdk.TGClient
import com.touchgui.sdk.TGConnectionListener
import com.touchgui.sdk.TGOTAManager
import com.touchgui.sdk.bean.TGBindResult
import timber.log.Timber
import java.lang.reflect.Method
import javax.inject.Inject


class Evolve2ConnectHandler
@Inject
constructor(val evolve2ApplicationHandler: Evolve2ApplicationHandler) : ConnectionDataActions() {
    private var baseConnectionCallbacks: ConnectionCallbacks? = null
    private val TAG = Evolve2ConnectHandler::class.simpleName
    private var noiseFitDevice: ColorFitDevice? = null
    private var mClient: TGClient? = null
    private var isReconnect = false

    init {
        mClient = evolve2ApplicationHandler.getTGBleClient()
    }

    override fun attachCallbacks() {
        mClient?.addConnectionListener(mConnectorListener)
    }

    override fun removeCallbacks() {
        mClient?.removeConnectionListener(mConnectorListener)
    }

    override fun <T> callbackListener(callback: T) {
        baseConnectionCallbacks = callback as ConnectionCallbacks
    }


    override fun <T> callbackListenerNew(callback: T) {

    }

    override fun connect(noiseFitDevice: ColorFitDevice) {
        LOGS.d("$TAG inside connect")
        //this.isBinding = false;
        isReconnect = false
        connectDevice(noiseFitDevice)
    }

    private fun connectDevice(noiseFitDevice: ColorFitDevice) {
        AppLogs.sendAppLogs("ConnectDevice : " + noiseFitDevice.address)
        baseConnectionCallbacks?.onConnect(ConnectState.Connecting(noiseFitDevice,
            ConnectionEventsConstants.Connecting))
        this.noiseFitDevice = noiseFitDevice
        LOGS.d("$TAG inside connectDevice $noiseFitDevice")
        if (mClient != null) {
            mClient?.connect(noiseFitDevice.address) //连接设备 Connect the device
        } else {
            LOGS.d("$TAG inside connectDevice Please connect")
            AppLogs.sendAppLogs("TGBleClient is null reconnecting")
            mClient = evolve2ApplicationHandler.openBleService()
            Handler(Looper.getMainLooper()).postDelayed({
                //attachCallbacks();
                connectDevice(noiseFitDevice) //重新调用连接 Recall the connection
            }, 2000)
        }
    }

    private val mConnectorListener: TGConnectionListener = object : TGConnectionListener {


        override fun onConnectionStateChange(p0: Int, p1: String?) {
            LOGS.d("$TAG onConnectionStateChange  $p0 $p1")
        }

        override fun onReady(
            name: String?,
            address: String,
            versionCode: Int,
            needForceOTA: Boolean
        ) {
            LOGS.d("$TAG Connection State onReady , needForceOTA > $needForceOTA $versionCode")

            if (needForceOTA) {
                noiseFitDevice?.let {
                    baseConnectionCallbacks?.onConnect(
                        ConnectState.DfuMode(
                            it,
                            versionCode,
                            needForceOTA
                        )
                    )
                }
                AppLogs.sendAppLogs("needForceOTA")
            } else {
                mClient!!.commandBuilder.bind().execute(object : TGCallback<TGBindResult> {
                    override fun onSuccess(data: TGBindResult?) {
                        if (data?.result != 0) {
                            LOGS.d("$TAG Bind fail")
                            bindFailed()
                            if (isReconnect) {
                                disconnect(noiseFitDevice!!)
                            } else {
                                mClient?.disconnect()
                            }

                        } else {

                            LOGS.d("$TAG connect success")
                            noiseFitDevice?.let {
                                baseConnectionCallbacks?.onConnect(
                                    ConnectState.DfuMode(
                                        it,
                                        versionCode,
                                        needForceOTA
                                    )
                                )
                            }
                            AppLogs.sendAppLogs("Connect Success")
                            connectSuccess()

                        }
                    }

                    override fun onFailure(throwable: Throwable) {
                        LOGS.d("$TAG connect onFailure")
                        throwable.printStackTrace()
                        bindFailed()
                        mClient?.disconnect() //发送恢复出厂指令 Send factory reset command
//                      ;l  disconnectSuccess()
                    }


                })
            }
        }

        override fun onError(i: Int) {
            LOGS.d("$TAG connect onFailure")
            bindFailed()
        }
    }

    override fun disconnect(noiseFitDevice: ColorFitDevice) {

        mClient?.commandBuilder?.unbind(false)?.execute(object : TGCallback<Void?> {
            override fun onSuccess(data: Void?) {
//                mClient?.commandBuilder?.reset()
                LOGS.d("$TAG Disconnect success")
                //if(mClient?.isConnected == true)
                //   mClient?.disconnect() //发送恢复出厂指令 Send factory reset command
                disconnectSuccess()
            }

            override fun onFailure(throwable: Throwable) {}
        })
    }

    override fun reconnect(noiseFitDevice: ColorFitDevice, type: Boolean) {
        isReconnect = type

        if (mClient == null) {
            throw NullPointerException("Client can't be null")
        }

        val isConnected = isConnected()

        val isReconnecting = isReconnecting()

        if (isReconnecting) {
            LOGS.d("$TAG hold on watch is trying to reconnect again")
            return
        }


        LOGS.d("$TAG reconnect $isConnected $isReconnecting")
        if (!isConnected) {
            mClient?.addConnectionListener(mConnectorListener)
            connectDevice(noiseFitDevice)
        } else {
            LOGS.d("$TAG please reconnect $isConnected")
            AppLogs.sendAppLogs("Reconnect Success")
            connectSuccess()
        }
    }


    override fun isDevicePaired(noiseFitDevice: ColorFitDevice): Boolean {
        this.noiseFitDevice = noiseFitDevice
        return true
    }


    private fun connectSuccess() {

        noiseFitDevice?.let {
            baseConnectionCallbacks?.onConnect(ConnectState.ConnectSuccess(it,ConnectionEventsConstants.Success))
            baseConnectionCallbacks?.onDeviceReady(it)
        }
    }

    private fun bindFailed() {
        AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.Failed)

        noiseFitDevice?.let {
            baseConnectionCallbacks?.onConnect(ConnectState.ConnectFailed(it,ConnectionEventsConstants.Failed))

        }
    }

    private fun disconnectSuccess() {
        AppLogs.sendAppLogs("Disconnect Success")
        isReconnect = false
        noiseFitDevice?.address?.let { removeBond(it) }
        baseConnectionCallbacks?.onConnect(ConnectState.DisconnectSuccess(noiseFitDevice,ConnectionEventsConstants.Disconnect_success))
    }

    fun removeBond(address: String): Boolean {
        val defaultAdapter = BluetoothAdapter.getDefaultAdapter()
        val remoteDevice = defaultAdapter.getRemoteDevice(address)
        try {
            val method: Method = remoteDevice.javaClass.getDeclaredMethod("removeBond")
            method.isAccessible = true
            method.invoke(remoteDevice)
            return true
        } catch (e: Exception) {
            Timber.e(e)
        }
        return false
    }


    override fun isConnected(): Boolean {
        LOGS.d("$TAG isConnected__ ${mClient?.isConnected}")
        return mClient?.isConnected ?: false
    }

    private fun isReconnecting(): Boolean {
        return mClient?.isReconnecting ?: false
    }

    override fun startDfuUpdate(fileUri: String) {
        val filePath = fileUri.split("///").last()
        val manager: TGOTAManager? = mClient?.otaManager
        manager?.setCallback(object : TGOTAManager.OTACallback {
            override fun onProgress(p0: Int) {
                baseConnectionCallbacks?.onFirmwareUpgradeProgress(
                    DeviceFirmware(
                        status = "progress",
                        percentage = p0
                    )
                )
            }

            override fun onCompleted() {
                baseConnectionCallbacks?.onFirmwareUpgradeProgress(
                    DeviceFirmware(
                        status = "success",
                        version = null
                    )
                )
            }

            override fun onError(p0: Throwable?) {
                p0?.printStackTrace()
                baseConnectionCallbacks?.onFirmwareUpgradeProgress(
                    DeviceFirmware(
                        status = "error",
                        message = p0?.message
                    )
                )
            }
        })
        manager?.start(filePath, false);
    }


}