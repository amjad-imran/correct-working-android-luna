package com.noisefit_zhsdk.handler

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.noisefit_commans.constants.ConnectionEventsConstants
import com.noisefit_commans.constants.ConnectionEventsConstants.Connecting
import com.noisefit_commans.constants.ConnectionEventsConstants.Disconnected
import com.noisefit_commans.constants.ConnectionEventsConstants.Timeout
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.connection.ConnectionCallbacks
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.connection.ResetStates
import com.noisefit_commans.interfaces.connection.WatchBindState
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.models.BatteryData
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceFirmware
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.ConnectEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import com.noisefit_zhsdk.base.ZhApplicationHandler
import com.zhapp.ble.BleBCManager
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.BindDeviceBean
import com.zhapp.ble.bean.DeviceInfoBean
import com.zhapp.ble.callback.BindDeviceStateCallBack
import com.zhapp.ble.callback.BleStateCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceInfoCallBack
import com.zhapp.ble.callback.DisconnectReasonCallBack
import com.zhapp.ble.callback.RequestDeviceBindStateCallBack
import com.zhapp.ble.callback.UnbindDeviceCallBack
import com.zhapp.ble.callback.VerifyUserIdCallBack
import com.zhapp.ble.callback.ZHInitStatusCallBack
import com.zhapp.ble.parsing.ParsingStateManager
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

private const val TAG = "ZhConnectHandler"

class ZhConnectHandler
@Inject
constructor(val zhApplicationHandler: ZhApplicationHandler) : ConnectionDataActions() {


    private var noiseFitDevice: ColorFitDevice? = null
    private var isReconnect = false
    private var isCallingBind = false
    private var isDisconnect = false
    private var baseConnectionCallbacks: ConnectionCallbacks? = null
    private val bindDeviceConnectDeviceTotalTimes = 4
    private var bindDeviceConnectDeviceCount = 0

    private var controlBleTools: ControlBleTools? = null


    override fun <T> callbackListener(callback: T) {
        baseConnectionCallbacks = callback as ConnectionCallbacks
    }

    override fun init() {
        super.init()
        AppLogs.sendAppLogs("$TAG : scanCallBack init called ")
        controlBleTools = zhApplicationHandler.getZhService()
    }


    private val connectionCallback = object : BleStateCallBack {
        override fun onConnectState(state: Int) {
            LOGS.d(TAG, "onConnectState $state")
            when (state) {
                BleCommonAttributes.STATE_CONNECTED -> {
                    AppLogs.sendAppLogs("onConnectState connected ${controlBleTools?.isConnect}")
//                    if (controlBleTools?.isConnect!!) {
                    controlBleTools?.requestDeviceBindState(null)
                    if (noiseFitDevice?.deviceType.equals(
                            DeviceType.NOISEFIT_LUNA.deviceType,
                            true
                        )
                    ) {
                        ControlBleTools.getInstance().getAutoSportData(null)
                        //ControlBleTools.getInstance().realTimeDataSwitch(true, null)

                    } else {
                        //ControlBleTools.getInstance().realTimeDataSwitch(true, null)
                    }
//                    }

                }

                BleCommonAttributes.STATE_CONNECTING -> {
                    AppLogs.sendAppLogs("$TAG : onConnectState Connecting")

                    baseConnectionCallbacks?.onConnect(
                        ConnectState.Connecting(
                            noiseFitDevice,
                            Connecting
                        )
                    )
                }

                BleCommonAttributes.STATE_TIME_OUT -> {
                    AppLogs.sendAppLogs("$TAG :onConnectState TimeOut")
                    baseConnectionCallbacks?.onConnect(
                        ConnectState.ConnectFailed(
                            noiseFitDevice,
                            Timeout
                        )
                    )
                }

                BleCommonAttributes.STATE_DISCONNECTED -> {
                    AppLogs.sendAppLogs("$TAG :onConnectState disconnected")

                    if (!noiseFitDevice?.watchToken.isNullOrEmpty() && !isDisconnect) {
                        return
                    }
                    baseConnectionCallbacks?.onConnect(
                        ConnectState.ConnectFailed(
                            noiseFitDevice,
                            Disconnected
                        )
                    )
                    isReconnect =
                        !ControlBleTools.getInstance().currentDeviceName.isNullOrEmpty() &&
                                !ControlBleTools.getInstance().currentDeviceMac.isNullOrEmpty() && !isDisconnect
                    LOGS.i(
                        TAG,
                        "onDisconnect isReconnect : $isReconnect isDisconnect $isDisconnect"
                    )

                    if (isReconnect) {
                        //SDK会一直重连 SDK will keep reconnecting
                        //APP处理-需要更新 UI APP handling-UI needs to be updated
                    } else {
                        //累加连接失败次数 Accumulate the number of connection failures
                        if (isDisconnect) {
                            AppLogs.sendAppLogs(
                                LogEvents.Connect,
                                ConnectEvents.Failed.apply { comment = "STATE_DISCONNECTED" })
                            disconnectSuccess()
                            return
                        }
                        bindDeviceConnectDeviceCount++
                        //当连接失败次数，满足条件后 When the number of connection failures, after the conditions are met
                        if (bindDeviceConnectDeviceCount >= bindDeviceConnectDeviceTotalTimes) {
                            AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.Failed)
                            bindFailed()
                        }
                    }
                }

            }
        }
    }


    private fun setConnectionCallback() {
        controlBleTools?.setBleStateCallBack(connectionCallback)
        //LOGS.d(TAG, "Callback state ${controlBleTools?.bleStateCallBack}")
    }

    fun getDisconnectReason() {
        LOGS.d("inside disconnection")
        CallBackUtils.disconnectReasonCallBack =
            DisconnectReasonCallBack { deviceInfoBean ->
                LOGS.d(
                    "disconnection reason $deviceInfoBean \n" +
                            "time: ${deviceInfoBean?.lastDisconnectTimestamp} ," +
                            "reason: ${deviceInfoBean?.lastDisconnectReason} , \n " +
                            "log: " + "${deviceInfoBean?.sdkLastDisconnectLog}"
                )
                AppLogs.sendAppLogs(
                    "disconnection reason $deviceInfoBean \n" +
                            "time: ${deviceInfoBean?.lastDisconnectTimestamp} ," +
                            "reason: ${deviceInfoBean?.lastDisconnectReason} , \n " +
                            "log: " + "${deviceInfoBean?.sdkLastDisconnectLog}"
                )
                //AppLogs.sendAppLogs("disconnection reason $deviceInfoBean ")


            }
        if (ControlBleTools.getInstance().isConnect) {
            ControlBleTools.getInstance().getDisconnectReason(null)
        }

    }

    override fun attachCallbacks() {
        AppLogs.sendAppLogs("scanCallBack attach listener start body: ")
        if (controlBleTools != null) {
            setConnectionCallback()
            initBindListener()
            initDeviceUnbindListener()
            AppLogs.sendAppLogs("$TAG : Listeners Attached")
        }
    }


    private fun initDeviceUnbindListener() {
        CallBackUtils.unbindDeviceCallBack = object : UnbindDeviceCallBack {
            override fun unbindDeviceSuccess() {
                AppLogs.sendAppLogs("$TAG  unbindDeviceSuccess")
                isDisconnect = true
                controlBleTools?.disconnect()
            }
        }

    }


    private fun initBindListener() {
        AppLogs.sendAppLogs(
            LogEvents.Binding,
            ConnectEvents.Other.apply { comment = "initBindListener" })


        CallBackUtils.requestDeviceBindStateCallBack = RequestDeviceBindStateCallBack { state ->
            LOGS.d("$TAG Bind State $state , isReconnect $isReconnect")
            AppLogs.sendAppLogs("Bind State $state , isReconnect $isReconnect")
            if (state) {
                AppLogs.sendAppLogs(LogEvents.Binding, ConnectEvents.Other.apply {
                    comment = "bind reconnect"
                })
                CallBackUtils.verifyUserIdCallBack = VerifyUserIdCallBack {
                    LOGS.d("$TAG verifyUserIdCallBack state=$it")
                    /*connectSuccess()
                    return@VerifyUserIdCallBack*/
                    //verify success
                    if (it == 0) {
                        connectSuccess()
                    } else {
                        bindFailed()

                        if (noiseFitDevice?.watchToken.isNullOrEmpty()) {
                            baseConnectionCallbacks?.onConnect(
                                ConnectState.ReconnectStatus(
                                    WatchBindState.AlreadyPaired
                                )
                            )
                        } else {
                            baseConnectionCallbacks?.onConnect(
                                ConnectState.ReconnectStatus(
                                    WatchBindState.InvalidToken
                                )
                            )
                        }
                    }
                }
                LOGS.d("$TAG verifyUserId token=${noiseFitDevice?.watchToken}")
                ControlBleTools.getInstance().verifyUserId(noiseFitDevice?.watchToken, null)
            } else {
                if (noiseFitDevice?.watchToken.isNullOrEmpty()) {
                    if (isReconnect) {
                        noiseFitDevice?.let { disconnect(it) }
                    } else {
                        bindDevice()
                    }
                } else {
                    LOGS.d("$TAG bindstatus failed")
                    isReconnect = false
                    controlBleTools?.disconnect()
                    noiseFitDevice?.let { removeBond(it) }
                    if (!isCallingBind) {
                        AppLogs.sendAppLogs(
                            LogEvents.Connect,
                            ConnectEvents.Failed.apply {
                                comment =
                                    "STATE_DISCONNECTED Bind State->$state isReconnect->$isReconnect"
                            })
                        disconnectSuccess()
                    }
                    baseConnectionCallbacks?.onConnect(
                        ConnectState.ReconnectStatus(
                            WatchBindState.WatchIsUnbind
                        )
                    )
                }
            }
        }
    }

    private fun bindDevice() {
        LOGS.d("$TAG bindDevice_called")

        CallBackUtils.bindDeviceStateCallBack = object : BindDeviceStateCallBack {
//            override fun onDeviceInfo(
//                serialNumber: String,
//                deviceNumber: String,
//                bindCheckResult: Boolean,
//                firmwareVersion: String,
//                mac: String
//            ) {
//
//            }

            //BindDeviceBean{deviceVerify=true, serialNumber='10005', deviceNumber='30023', firmwareVersion='1.1.2', name='NoiseFit Twist_6921', mac='d8:2b:16:07:69:21'}
            override fun onDeviceInfo(bindDeviceBean: BindDeviceBean) {
                LOGS.d("$TAG bindCheckResult : ${bindDeviceBean.deviceVerify} ")
                if (bindDeviceBean.deviceVerify) {
                    val userId = "${UUID.randomUUID()}${Random.nextInt(0, 1000)}"
                    controlBleTools?.sendAppBindResult(
                        userId,
                        object : ParsingStateManager.SendCmdStateListener() {
                            override fun onState(sendCmdState: SendCmdState?) {
                                LOGS.d("$TAG sendAppBindResult  onState: $sendCmdState ")
                                if (sendCmdState == SendCmdState.SUCCEED) {
                                    noiseFitDevice!!.watchToken = userId
                                    connectSuccess()
                                    AppLogs.sendAppLogs(
                                        LogEvents.Binding,
                                        ConnectEvents.Other.apply {
                                            comment = "Success"
                                        })
                                } else {
                                    bindFailed()
                                }
                            }
                        })
                } else {
                    bindFailed()
                }
            }
        }
        controlBleTools?.bindDevice(null)
    }

    private fun bindFailed() {
        isDisconnect = true
        AppLogs.sendAppLogs(LogEvents.Binding, ConnectEvents.Failed)
        controlBleTools?.disconnect()
    }

    override fun <T> callbackListenerNew(callback: T) {
    }

    override fun removeCallbacks() {
        baseConnectionCallbacks = null
        //zhApplicationHandler.getConnectState().removeObserver(connectionObserver)

    }


    override fun connect(noiseFitDevice: ColorFitDevice) {
        isCallingBind = true
        isReconnect = false
        isDisconnect = false
        LOGS.d("$TAG connect")
        connectDevice(noiseFitDevice)
    }

    override fun disconnect(noiseFitDevice: ColorFitDevice) {
        isReconnect = false
        LOGS.d("$TAG disconnect   isCOnnected ${controlBleTools?.isConnect}")
        isDisconnect = true
        if (controlBleTools?.isConnect!!) {
            controlBleTools?.unbindDevice(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    LOGS.d(TAG, "onState $state ")
                    Handler(Looper.myLooper()!!).postDelayed({
                        controlBleTools?.disconnect()
                        removeBond(noiseFitDevice)
                    }, 1000)
                }
            })
        } else {
            Handler(Looper.myLooper()!!).postDelayed({
                controlBleTools?.disconnect()
                removeBond(noiseFitDevice)
            }, 1000)
        }
    }

    override fun forceDisconnect(
        noiseFitDevice: ColorFitDevice,
        callback: (states: ResetStates) -> Unit
    ) {
        callback.invoke(ResetStates.STARTED)
        controlBleTools?.connect(
            noiseFitDevice.bluetoothName!!,
            noiseFitDevice.address!!
        )
        CallBackUtils.deviceInfoCallBack = object : DeviceInfoCallBack {

            override fun onBatteryInfo(capacity: Int, chargeStatus: Int) {

                var isCharging = false
                if (chargeStatus == 1) {
                    isCharging = true
                }
                LOGS.d("onBatteryInfo $capacity $chargeStatus $isCharging")

                if (isCharging || capacity == 100) {

                    controlBleTools?.unbindDevice(object : SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            LOGS.d(TAG, "forceDisconnect onState $state ")
                            Handler(Looper.myLooper()!!).postDelayed({
                                controlBleTools?.disconnect()
                                callback.invoke(ResetStates.RESET_SUCCESS)
                            }, 1000)
                        }
                    })
                } else {
                    callback.invoke(ResetStates.NOT_ON_CHARGING)
                    isDisconnect = true
                    controlBleTools?.disconnect()
                }

            }

            override fun onDeviceInfo(deviceInfoBean: DeviceInfoBean) {

            }
        }


        controlBleTools?.setBleStateCallBack(object : BleStateCallBack {
            override fun onConnectState(state: Int) {
                LOGS.d(TAG, "forceDisconnect onConnectState $state")
                when (state) {
                    BleCommonAttributes.STATE_CONNECTED -> {
                        callback.invoke(ResetStates.CONNECTED)
                        controlBleTools?.getDeviceBattery(null)
                    }

                    BleCommonAttributes.STATE_CONNECTING -> {
                        AppLogs.sendAppLogs("$TAG : forceDisconnect onConnectState Connecting")

                    }

                    BleCommonAttributes.STATE_TIME_OUT -> {
                        callback.invoke(ResetStates.CONNECTION_FAILED)
                        AppLogs.sendAppLogs("$TAG : forceDisconnect onConnectState TimeOut")
                        isDisconnect = true
                        controlBleTools?.disconnect()

                    }

                    BleCommonAttributes.STATE_DISCONNECTED -> {
                        //callback.invoke(ResetStates.CONNECTION_FAILED)
                        AppLogs.sendAppLogs("$TAG : forceDisconnect disconnected")


                    }

                }
            }
        })

    }

    private fun removeBond(noiseFitDevice: ColorFitDevice) {
        //移除通话蓝牙配对 Remove call bluetooth pairing
        LOGS.d("$TAG removeBond")
        if (noiseFitDevice.isSupportHeadset && !TextUtils.isEmpty(noiseFitDevice.headsetMac)) {
            BleBCManager.getInstance().removeBond(noiseFitDevice.headsetMac)
        }
    }

    override fun reconnect(noiseFitDevice: ColorFitDevice, type: Boolean) {
        LOGS.i(TAG, "OnReConnect")
        isCallingBind = false
        isReconnect = type
        isDisconnect = false

        val isConnected = isConnected()

        if (!isConnected) {
            AppLogs.sendAppLogs("Reconnect not connect")
            this.noiseFitDevice = noiseFitDevice
            connectDevice(noiseFitDevice)
        } else {
            LOGS.i(TAG, "OnReConnect is connected")
            AppLogs.sendAppLogs("Reconnect connected")
            connectSuccess()
        }

    }

    private val bondListener = object : BleBCManager.BondListener {
        override fun onWaiting() {
            AppLogs.sendAppLogs("Bond on waiting")
        }

        override fun onBondError(p0: Exception?) {
            // p0?.printStackTrace()
            AppLogs.sendAppLogs("Bond on Error")
            connectDevice()
        }

        override fun onBonding() {
            AppLogs.sendAppLogs("Bond on bonding")
        }

        override fun onBondFailed() {
            LOGS.d(TAG, "onBluetoothConnect failed")
            baseConnectionCallbacks?.onBluetoothConnect(false)
            AppLogs.sendAppLogs("Bond on failed")
            //TODO 提示用户去系统蓝牙设置执行配对通话蓝牙  Prompt the user to go to the system Bluetooth settings to perform pairing call Bluetooth
        }

        override fun onBondSucceeded() {
            baseConnectionCallbacks?.onBluetoothConnect(true)
            noiseFitDevice?.let {
                if (it.isSupportHeadset && !TextUtils.isEmpty(it.headsetMac)) { //设备支持bt
                    if (BleBCManager.getInstance().checkBondByMac(it.headsetMac) &&
                        !BleBCManager.getInstance().isConnected(it.headsetMac)
                    ) {
                        //Execute the connection ,can leave the result unprocessed
                        BleBCManager.getInstance()
                            .connectHeadsetBluetoothDevice(it.headsetMac, null)
                    }
                }
            }
            AppLogs.sendAppLogs("BT Calling BluetoothDevice Bond on Succeeded")
        }

    }


    private fun connectDevice() {
        //if(controlBleTools?.isConnecting == true) return
        controlBleTools?.connect(
            noiseFitDevice!!.bluetoothName!!,
            noiseFitDevice!!.address!!
        )

        AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.Other.apply { comment = "Connect" })
    }

    private fun connectDevice(noiseFitDevice: ColorFitDevice) {
        isDisconnect = false
        baseConnectionCallbacks?.onConnect(ConnectState.Connecting(noiseFitDevice, Connecting))
        this.noiseFitDevice = noiseFitDevice

        AppLogs.sendAppLogs("ConnectDevice " + noiseFitDevice.address)
        if (controlBleTools != null && controlBleTools!!.isInit) {
            bindDeviceConnectDeviceCount = 0

            LOGS.i(
                TAG,
                "ConnectDevice the device $noiseFitDevice"
            )
            connectDevice()
        } else {
            if (controlBleTools == null) controlBleTools = zhApplicationHandler.getZhService()

            controlBleTools?.setInitStatusCallBack(object : ZHInitStatusCallBack {
                override fun onInitComplete() {
                    attachCallbacks()
                    connectDevice(noiseFitDevice)
                }
            })
            zhApplicationHandler.initSdk()
        }
    }


    override fun isDevicePaired(noiseFitDevice: ColorFitDevice): Boolean {
        this.noiseFitDevice = noiseFitDevice
        return true
    }

    private fun connectSuccess() {
        noiseFitDevice?.let { colorFitDevice ->
            baseConnectionCallbacks?.onConnect(
                ConnectState.ConnectSuccess(
                    colorFitDevice,
                    ConnectionEventsConstants.Success
                )
            )
            baseConnectionCallbacks?.onDeviceReady(noiseFitDevice)
            if (ControlBleTools.getInstance().isConnect && isReconnect) {
                LOGS.d("SetDeviceTime")
                AppLogs.sendAppLogs("Set time success")
                ControlBleTools.getInstance().setTime(System.currentTimeMillis(), null)
                getDisconnectReason()
            }
            //执行通话蓝牙配对 Perform call bluetooth pairing
            LOGS.i(
                TAG,
                "isSupportHeadset:${colorFitDevice.isSupportHeadset}, bt mac:${colorFitDevice.headsetMac}, isbind:${colorFitDevice.isBind}"
            )
            if (colorFitDevice.isSupportHeadset && !TextUtils.isEmpty(colorFitDevice.headsetMac)) { //设备支持bt
                if (!isReconnect) {
                    if (!colorFitDevice.isBind && !BleBCManager.getInstance()
                            .checkBondByMac(colorFitDevice.headsetMac)
                    ) {
                        LOGS.i(TAG, "call bluetooth pairing")
                        AppLogs.sendAppLogs("call bluetooth pairing")
                        BleBCManager.getInstance().createBond(
                            colorFitDevice.headsetMac,
                            bondListener
                        )
                    } else {
                        //Execute the connection ,can leave the result unprocessed
                        BleBCManager.getInstance()
                            .connectHeadsetBluetoothDevice(colorFitDevice.headsetMac, null)
                    }
                }
            }
        } ?: LOGS.i(TAG, "noiseFitDevice Should not be null ")
        AppLogs.sendAppLogs("Device connection success")
    }

    private fun disconnectSuccess() {
        //LOGS.d("$TAG  disconnectSuccess ${Gson().toJson(noiseFitDevice)}")
        AppLogs.sendAppLogs("Device disconnect success")
        baseConnectionCallbacks?.onConnect(
            ConnectState.DisconnectSuccess(
                noiseFitDevice,
                ConnectionEventsConstants.Disconnect_success
            )
        )
        LOGS.i(TAG, "Disconnect success called")
    }

    override fun isConnected(): Boolean {
        LOGS.d("$TAG ${controlBleTools?.isConnect} ${controlBleTools?.isConnecting}")
        val isConnected = controlBleTools?.isConnect ?: false
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