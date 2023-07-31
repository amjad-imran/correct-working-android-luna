package com.noisefit_nav_plus.handler.connect

import android.os.Handler
import android.os.Looper
import com.noisefit_commans.constants.ConnectionEventsConstants
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.connection.ConnectionCallbacks
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.utils.*
import com.noisefit_nav_plus.base.NavPlusApplicationHandler
import com.zjw.zhbraceletsdk.linstener.BindDeviceListener
import com.zjw.zhbraceletsdk.linstener.ConnectorListener
import com.zjw.zhbraceletsdk.linstener.ZHInitStatusListener
import com.zjw.zhbraceletsdk.service.ZhBraceletService
import javax.inject.Inject

class NavPlusConnectHandler
@Inject
constructor(val navPlusApplicationHandler: NavPlusApplicationHandler) : ConnectionDataActions() {

    private val TAG = "NavPlusConnectHandler"

    private var noiseFitDevice: ColorFitDevice? = null
    private var mBleService: ZhBraceletService? = null
    private var isReconnect = false
    private var isDisconnect = false
    private var baseConnectionCallbacks: ConnectionCallbacks? = null
    private val bindDeviceConnectDeviceTotalTimes = 4
    private var bindDeviceConnectDeviceCount = 0
    override fun <T> callbackListener(callback: T) {
        baseConnectionCallbacks = callback as ConnectionCallbacks
    }

    override fun init() {
        super.init()
        LOGS.i(TAG, "scanCallBack init called ")
        mBleService = navPlusApplicationHandler.getZhBraceletService()

    }


    override fun attachCallbacks() {
        mBleService = navPlusApplicationHandler.getZhBraceletService()
        if (mBleService != null) {
            attachListener()
        }
    }

    override fun <T> callbackListenerNew(callback: T) {
    }

    private fun attachListener() {
        LOGS.i(TAG, "scanCallBack attach listener start body: ")
        if (mBleService != null) {
            mBleService?.addConnectorListener(mConnectorListener)
            initBindListener()
            initDeviceUnbindListener()
            LOGS.i(TAG, "Listeners Attached")
        }
    }

    override fun removeCallbacks() {
        mBleService?.removeConnectorListener(mConnectorListener)
    }

    private fun connectDevice(noiseFitDevice: ColorFitDevice) {
        isDisconnect = false
        baseConnectionCallbacks?.onConnect(ConnectState.Connecting(noiseFitDevice,
            ConnectionEventsConstants.Connecting
        ))
        this.noiseFitDevice = noiseFitDevice
        LOGS.i(TAG, "ConnectDevice" + noiseFitDevice.address)
        //服务不为空 Service is not empty
        if (mBleService != null) {
            //重置次数 Number of resets
            bindDeviceConnectDeviceCount = 0
            LOGS.i(TAG, "ConnectDevice the device $bindDeviceConnectDeviceCount")
            AppLogs.sendAppLogs("ConnectDevice : " + noiseFitDevice.address)
            mBleService?.connectDevice(noiseFitDevice.address) //连接设备 Connect the device
        } else {
            //重新打开服务 Reopen service
            LOGS.i(TAG, "mBleService is null reconnecting")
            AppLogs.sendAppLogs("mBleService is null reconnecting")
            navPlusApplicationHandler.openBleService(object : ZHInitStatusListener {
                override fun onInitComplete() {
                    attachCallbacks()
                    connectDevice(noiseFitDevice) //重新调用连接 Recall the connection
                }
            })
        }
    }


    override fun connect(noiseFitDevice: ColorFitDevice) {
        isReconnect = false
        connectDevice(noiseFitDevice)
    }

    private val mConnectorListener: ConnectorListener = object : ConnectorListener {
        override fun onConnect() {
            LOGS.i(TAG, "ConnectDevice the device onConnect isReconnect : $isReconnect")
            if (isReconnect) {

                mBleService?.bindDevice(1) //询问绑定状态 Ask for binding status
                LOGS.i(TAG, "ConnectDevice the device mBleService?.bindDevice(1)")
            } else {

                AppLogs.sendAppLogs("Initiate Binding")
                mBleService?.bindDevice(0) //发起绑定 Initiate binding
                LOGS.i(TAG, "ConnectDevice the device mBleService?.bindDevice(0)")
            }
        }

        override fun onDisconnect() {
            LOGS.i(TAG, "ConnectDevice the device onDisconnect")
            //重连 Reconnection
            baseConnectionCallbacks?.onConnect(ConnectState.ConnectFailed(noiseFitDevice,ConnectionEventsConstants.Disconnected))
            LOGS.i(TAG, "onDisconnect $isReconnect")

            if (isReconnect) {
                //SDK会一直重连 SDK will keep reconnecting
                //APP处理-需要更新 UI APP handling-UI needs to be updated
            } else {
                //累加连接失败次数 Accumulate the number of connection failures
                if (isDisconnect) {
                    disconnectSuccess()
                    return
                }
                bindDeviceConnectDeviceCount++
                //当连接失败次数，满足条件后 When the number of connection failures, after the conditions are met
                if (bindDeviceConnectDeviceCount >= bindDeviceConnectDeviceTotalTimes) {
                    bindFailed()
                    AppLogs.sendAppLogs(LogEvents.Connect,ConnectEvents.Failed)
                }
            }
        }

        override fun onConnectFailed() {
            LOGS.i(TAG, "ConnectDevice the device onConnectFailed")
            AppLogs.sendAppLogs(LogEvents.Connect,ConnectEvents.Failed)
            bindFailed()
        }
    }

    private fun bindFailed() {
        disconnectSuccess()
        mBleService?.disconnectDevice()
    }

    private fun initBindListener() {
        mBleService?.setBindListener(object : BindDeviceListener {
            override fun bindSuccess() {
                AppLogs.sendAppLogs("Connect Success")
                connectSuccess()
            }

            override fun deniedByDevice() {
                AppLogs.sendAppLogs(LogEvents.Connect,ConnectEvents.DeniedByDevice)
                bindFailed()
                mBleService?.disconnectDevice()
                baseConnectionCallbacks?.onConnect(ConnectState.ConnectFailed(noiseFitDevice))
            }

            override fun bindError() {
                AppLogs.sendAppLogs(LogEvents.Connect,ConnectEvents.Failed)
                bindFailed()
            }
        })
    }

    private fun initDeviceUnbindListener() {
        mBleService?.setDeviceUnbindListener {
            AppLogs.sendAppLogs("Device unbind Success")
            disconnectSuccess()
            mBleService?.disconnectDevice()
        }
    }

    override fun disconnect(noiseFitDevice: ColorFitDevice) {
        if (mBleService != null) {
            isReconnect = false
            mBleService?.factorySetting()
            isDisconnect = true
            //disconnectSuccess()
            Handler(Looper.getMainLooper()).postDelayed({
                AppLogs.sendAppLogs("Disconnect Device")
                mBleService?.disconnectDevice()
            }, 2000)
        }
    }

    override fun reconnect(noiseFitDevice: ColorFitDevice, type: Boolean) {
        LOGS.i(TAG, "OnReConnect")

        //isBinding = false;
        isReconnect = type
        isDisconnect = false
        val isConnected = isConnected()

        if (!isConnected) {
            LOGS.i(TAG, "OnReConnect is not connected")
            this.noiseFitDevice = noiseFitDevice
            connectDevice(noiseFitDevice)
        } else {
            LOGS.i(TAG, "OnReConnect is connected")
            connectSuccess()
            AppLogs.sendAppLogs("Reconnect Success")
        }


    }

    override fun isDevicePaired(noiseFitDevice: ColorFitDevice): Boolean {
        this.noiseFitDevice = noiseFitDevice
        return true
    }

    private fun connectSuccess() {
        noiseFitDevice?.let {
            baseConnectionCallbacks?.onConnect(ConnectState.ConnectSuccess(it,ConnectionEventsConstants.Success))
            baseConnectionCallbacks?.onDeviceReady(noiseFitDevice)
        } ?: LOGS.i(TAG, "noiseFitDevice Should not be null ")

    }

    private fun disconnectSuccess() {
        AppLogs.sendAppLogs("Disconnect Success")
        baseConnectionCallbacks?.onConnect(ConnectState.DisconnectSuccess(noiseFitDevice,ConnectionEventsConstants.Disconnect_success))
    }

    override fun isConnected(): Boolean {
        val isConnected = mBleService?.bleConnectState ?: false
        if (!isConnected) {
            baseConnectionCallbacks?.onConnect(
                ConnectState.ConnectFailed(
                    noiseFitDevice,
                    ConnectionEventsConstants.Failed
                )
            )
        }
        return isConnected
    }

    override fun onConnectedQRBinding() {
        super.onConnectedQRBinding()
        LOGS.i(TAG, "scanCallBack onConnectedQRBinding start body: ")
    }
}