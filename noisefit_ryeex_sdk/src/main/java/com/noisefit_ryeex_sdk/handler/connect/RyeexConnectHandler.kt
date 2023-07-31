package com.noisefit_ryeex_sdk.handler.connect

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.text.format.DateUtils
import com.google.gson.Gson
import com.noisefit_commans.constants.ConnectionEventsConstants
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.connection.ConnectionCallbacks
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.connection.WatchBindState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler
import com.noisefit_ryeex_sdk.dataConversion.removeBond
import com.ryeex.ble.common.device.DeviceConnectListener
import com.ryeex.ble.common.device.OnBindListener
import com.ryeex.ble.common.device.OnUnbindListener
import com.ryeex.ble.common.model.entity.RyeexDeviceBindInfo
import com.ryeex.ble.connector.callback.AsyncBleCallback
import com.ryeex.ble.connector.error.BleError
import com.ryeex.ble.connector.error.BleErrorCode
import com.ryeex.ble.connector.error.ServerError
import com.ryeex.watch.adapter.device.WatchDevice
import com.ryeex.watch.bt.BTHelper
import com.ryeex.watch.bt.BTHelper.OnConnectCallback
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

private const val TAG = "RyeexConnectHandler"

class RyeexConnectHandler
@Inject
constructor(
    private val ryeexApplicationHandler: RyeexApplicationHandler,
    private val watchDataStore: WatchDataStore
) :
    ConnectionDataActions() {

    private var noiseFitDevice: ColorFitDevice? = null
    private var isReconnect = false
    private var isDisconnect = false
    private var baseConnectionCallbacks: ConnectionCallbacks? = null
    private var btHelper: BTHelper? = null

    override fun connect(noiseFitDevice: ColorFitDevice) {
        LOGS.i(TAG, "connect noiseFitDevice=$noiseFitDevice")
        AppLogs.sendAppLogs("$TAG connect noiseFitDevice=$noiseFitDevice")
        this.noiseFitDevice = noiseFitDevice
        isDisconnect = false

        val bindingDevice = WatchDevice()
        bindingDevice.mac = noiseFitDevice.address
        ryeexApplicationHandler.setWatchDevice(bindingDevice)
        bindingDevice.bind(object : OnBindListener {
            override fun onConnecting() {
                baseConnectionCallbacks?.onConnect(ConnectState.Connecting(noiseFitDevice,ConnectionEventsConstants.Connecting))
                LOGS.i(TAG, "bind onConnecting")
            }

            override fun onConfirming() {
                LOGS.i(TAG, "bind onConfirming")
            }

            override fun onBinding() {
                LOGS.i(TAG, "bind onBinding")
            }

            override fun onServerBind(
                p0: RyeexDeviceBindInfo?,
                bleCallback: AsyncBleCallback<Void, ServerError>?
            ) {
                LOGS.i(TAG, "bind onServerBind")
                bleCallback?.sendSuccessMessage(null)
            }

            override fun onSuccess() {
//                LOGS.d(TAG, "bind onSuccess ${WatchDevice().token}")
                LOGS.i(TAG, "bind onSuccess ${bindingDevice.token}")
                AppLogs.sendAppLogs("$TAG bind onSuccess mac=${bindingDevice.mac}")
                noiseFitDevice.watchToken = bindingDevice.token!!
                watchDataStore.setRyeexWatchToken(bindingDevice.token!!)
                setConnectListener(bindingDevice)

                if (!bindingDevice.isLogin) {
                    reConnectDevice(noiseFitDevice)
                }
//                connectSuccess()

                Timer("BtPair", false).schedule(3000) {
                    LOGS.i(TAG, "start BT pair")
                    AppLogs.sendAppLogs("$TAG start BT pair")
                    btHelper = BTHelper()
                    btHelper?.startPair(
                        bindingDevice.mac,
                        90 * DateUtils.SECOND_IN_MILLIS,
                        object : OnConnectCallback {
                            override fun onSuccess(mac: String) {
                                LOGS.i(TAG, "pair BT success mac=$mac")
                                AppLogs.sendAppLogs("$TAG pair BT success mac=$mac")

                            }

                            override fun onFailure(mac: String) {
                                LOGS.e(TAG, "pair BT fail mac=$mac")
                                AppLogs.sendAppLogs("$TAG pair BT fail mac=$mac")
                            }
                        })
                }

            }

            override fun onFailure(error: BleError?) {
                LOGS.d("$TAG onFailure ${error?.code}")
                bindFailed()
                if (error?.code == BleErrorCode.BLE_ALREADY_BIND_STATUS) {
                    baseConnectionCallbacks?.onConnect(ConnectState.ReconnectStatus(WatchBindState.AlreadyPaired))
                }
            }
        })
    }


    override fun disconnect(noiseFitDevice: ColorFitDevice) {
        btHelper?.cancelPair()
        isReconnect = false
        isDisconnect = true
        val watchDevice = ryeexApplicationHandler.getWatchDevice()
        if (watchDevice?.isLogin == true) {
            watchDevice.unbind(object : OnUnbindListener {
                override fun onServerUnbind(callback: AsyncBleCallback<Void, BleError>?) {
                    callback?.sendSuccessMessage(null)
                }

                override fun onSuccess() {
                    disconnectSuccess()
                }

                override fun onFailure(p0: BleError?) {
                    LOGS.d("$TAG Disconnect fail ${p0?.message}")
                }
            })
        } else {
            disconnectSuccess()
        }
    }

    override fun reconnect(noiseFitDevice: ColorFitDevice, type: Boolean) {
        LOGS.i(
            TAG,
            "reconnect type=$type token=${watchDataStore.getRyeexWatchToken()} noiseFitDevice=$noiseFitDevice"
        )
        isReconnect = type
        isDisconnect = false

        val isConnected = isConnected()
        LOGS.d("dsadsadsasda init")
        if (!isConnected) {
            LOGS.d("dsadsadsasda not connected")
            AppLogs.sendAppLogs("Reconnect not connect")
            reConnectDevice(noiseFitDevice)
        } else {
            LOGS.i(TAG, "OnReConnect is connected")
//            ryeexApplicationHandler.setWatchDevice(bindingDevice!!)
            AppLogs.sendAppLogs("Reconnect connected")
            connectSuccess()
        }

    }

    private fun reConnectDevice(noiseFitDevice: ColorFitDevice) {
        this.noiseFitDevice = noiseFitDevice
        isDisconnect = false
        baseConnectionCallbacks?.onConnect(ConnectState.Connecting(noiseFitDevice,ConnectionEventsConstants.Connecting))
        LOGS.d("dsadsadsasda inside")
        val watchDevice = ryeexApplicationHandler.getWatchDevice()
        //If it is not the same device, create a new one
        if (watchDevice == null || watchDevice.mac.isNullOrEmpty()) {
            val watchDeviceNew = WatchDevice()
            watchDeviceNew.mac = noiseFitDevice.address
            watchDeviceNew.token = noiseFitDevice.watchToken.ifEmpty {
                watchDataStore.getRyeexWatchToken()
            }
            setConnectListener(watchDeviceNew)
            LOGS.d("dsadsadsasda new device")
            ryeexApplicationHandler.setWatchDevice(watchDeviceNew)
        }
        ryeexApplicationHandler.getWatchDevice()
            ?.login(object : AsyncBleCallback<Void?, BleError>() {
                override fun onSuccess(result: Void?) {
                    LOGS.i(TAG, "login onSuccess")
                    LOGS.d("dsadsadsasda onSuccess")
                }

                override fun onFailure(error: BleError) {
                    LOGS.e(TAG, "login onFailure:$error")
                    LOGS.d("dsadsadsasda onFailure:$error")

                    if (error.code == BleErrorCode.BLE_TOKEN_WRONG) {
                        bindFailed()
                        baseConnectionCallbacks?.onConnect(
                            ConnectState.ReconnectStatus(
                                WatchBindState.InvalidToken
                            )
                        )
                    } else if (error.code == BleErrorCode.BLE_ALREADY_UNBIND_STATUS) {
                        baseConnectionCallbacks?.onConnect(
                            ConnectState.ReconnectStatus(
                                WatchBindState.WatchIsUnbind
                            )
                        )
                    }else{
                        bindFailed()
                    }


                }
            })
        AppLogs.sendAppLogs("ConnectDevice " + noiseFitDevice.address)

    }

    private fun setConnectListener(watchDevice: WatchDevice?) {
        watchDevice?.addDeviceConnectListener(object : DeviceConnectListener {
            override fun onConnecting() {
                AppLogs.sendAppLogs("$TAG deviceConnectListener onConnecting")
            }

            override fun onLoginSuccess() {
                AppLogs.sendAppLogs("$TAG deviceConnectListener onLoginSuccess")
                connectSuccess()
            }

            override fun onDisconnected(error: BleError?) {
                AppLogs.sendAppLogs("$TAG deviceConnectListener onDisconnected:$error")
                if (error?.code == BleErrorCode.BLE_ALREADY_UNBIND_STATUS) {
                    watchDevice.removeDeviceConnectListener()
                    watchDevice.unbind(null)
                    disconnectSuccess()
                }
            }

            override fun onFailure(error: BleError?) {
                AppLogs.sendAppLogs("$TAG deviceConnectListener onFailure: $error")
                if (error?.code == BleErrorCode.BLE_ALREADY_UNBIND_STATUS) {
                    watchDevice.removeDeviceConnectListener()
                    watchDevice.unbind(null)
                    disconnectSuccess()
                }
            }
        })
    }


    override fun isDevicePaired(noiseFitDevice: ColorFitDevice): Boolean {
        this.noiseFitDevice = noiseFitDevice
        return true
    }


    @SuppressLint("MissingPermission")
    private fun connectSuccess() {
        noiseFitDevice?.let {
            baseConnectionCallbacks?.onConnect(ConnectState.ConnectSuccess(it,ConnectionEventsConstants.Success))
            baseConnectionCallbacks?.onDeviceReady(noiseFitDevice)

            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val device = bluetoothAdapter.bondedDevices.find { bluetoothDevice ->
                bluetoothDevice.address == it.address
            }
            if (device != null) {
                BTHelper().connectBT(device.address, object : OnConnectCallback {
                    override fun onSuccess(mac: String) {
                        LOGS.i(TAG, "connectBT onSuccess mac=$mac")
                        AppLogs.sendAppLogs("$TAG connectBT onSuccess mac=$mac")
                    }

                    override fun onFailure(mac: String) {
                        LOGS.e(TAG, "connectBT onFailure mac=$mac")
                        AppLogs.sendAppLogs("$TAG connectBT onFailure mac=$mac")
                    }
                })
                return
            }
        } ?: LOGS.i(TAG, "noiseFitDevice Should not be null ")
    }

    private fun disconnectSuccess() {
//        LOGS.d(TAG, "disconnectSuccess ${Gson().toJson(noiseFitDevice)}")
        AppLogs.sendAppLogs("Device disconnect success")
        watchDataStore.setRyeexWatchToken("")
        noiseFitDevice?.removeBond()
        ryeexApplicationHandler.setWatchDevice(null)
        baseConnectionCallbacks?.onConnect(ConnectState.DisconnectSuccess(noiseFitDevice,ConnectionEventsConstants.Disconnect_success))
    }

    override fun isConnected(): Boolean {
        LOGS.d(
            TAG,
            "isConnected=${ryeexApplicationHandler.getWatchDevice()?.isConnected} isConnecting=${ryeexApplicationHandler.getWatchDevice()?.isConnecting}"
        )
        val isLogin = ryeexApplicationHandler.getWatchDevice()?.isLogin ?: false
        if (!isLogin) {
            baseConnectionCallbacks?.onConnect(
                ConnectState.ConnectFailed(noiseFitDevice,ConnectionEventsConstants.Failed)
            )
        }
        return isLogin
    }

    private fun bindFailed() {
        noiseFitDevice?.let {
            baseConnectionCallbacks?.onConnect(ConnectState.ConnectFailed(it))
        }
    }

    override fun <T> callbackListener(callback: T) {
        baseConnectionCallbacks = callback as ConnectionCallbacks
        ryeexApplicationHandler.connectionCallbacks = baseConnectionCallbacks
    }

    override fun <T> callbackListenerNew(callback: T) {

    }
}