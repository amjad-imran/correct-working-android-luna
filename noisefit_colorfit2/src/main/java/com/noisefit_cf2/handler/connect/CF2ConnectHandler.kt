package com.noisefit_cf2.handler.connect

import android.net.Uri
import android.util.Log
import com.ido.ble.BLEManager
import com.ido.ble.LocalDataManager
import com.ido.ble.bluetooth.connect.ConnectFailedReason
import com.ido.ble.bluetooth.device.BLEDevice
import com.ido.ble.callback.BindCallBack
import com.ido.ble.callback.ConnectCallBack
import com.ido.ble.callback.UnbindCallBack
import com.ido.ble.dfu.BleDFUConfig
import com.ido.ble.dfu.BleDFUState
import com.ido.ble.protocol.model.SportModeSortV3
import com.noisefit_cf2.base.ColorFit2ApplicationHandler
import com.noisefit_cf2.dataconversions.Colorfit2DeviceConverter
import com.noisefit_commans.constants.ConnectionEventsConstants
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.connection.ConnectionCallbacks
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceFirmware
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.ConnectEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import java.io.File
import javax.inject.Inject


class CF2ConnectHandler
@Inject
constructor(val applicationHandler: ColorFit2ApplicationHandler) : ConnectionDataActions() {

    private var noiseFitDevice: ColorFitDevice? = null
    private var isReconnect = false
    private var baseConnectionCallbacks: ConnectionCallbacks? = null
    private var bindDeviceConnectDeviceCount = 0
    private var bindDeviceConnectDeviceTotalTimes = 4
    override fun <T> callbackListener(callback: T) {
        baseConnectionCallbacks = callback as ConnectionCallbacks
    }

    override fun <T> callbackListenerNew(callback: T) {}

    override fun init() {
        super.init()
        try {
            applicationHandler.initSdk()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun connect(noiseFitDevice: ColorFitDevice) {
        AppLogs.sendAppLogs("ConnectDevice : " + noiseFitDevice.address)
        baseConnectionCallbacks?.onConnect(ConnectState.Connecting(noiseFitDevice,ConnectionEventsConstants.Connecting))
        this.noiseFitDevice = noiseFitDevice
        isReconnect = false
        try {
            LOGS.d("CF2 " + noiseFitDevice)
            bindDeviceConnectDeviceCount = 0
            BLEManager.connect(Colorfit2DeviceConverter.getBleFromColorfit2(noiseFitDevice))
        } catch (e: Exception) {
            e.printStackTrace()
            AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.Failed.apply {
                comment = "${e.message}"
            })

        }
    }

    override fun getConnectionTimerDelay(): Long = 15000


    override fun disconnect(noiseFitDevice: ColorFitDevice) {
        BLEManager.registerUnbindCallBack(unbindCallBack)
        unbind()
    }

    override fun reconnect(noiseFitDevice: ColorFitDevice, type: Boolean) {
        this.noiseFitDevice = noiseFitDevice
        baseConnectionCallbacks?.onConnect(ConnectState.Connecting(noiseFitDevice,ConnectionEventsConstants.Connecting))
        try {
            if (isConnected().not()) {
                isReconnect = true
                attachCallbacks()
                BLEManager.autoConnect(noiseFitDevice?.address)
            } else {
                AppLogs.sendAppLogs("Reconnect Success")
                onDeviceConnected()
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
        }
    }

    override fun isDevicePaired(noiseFitDevice: ColorFitDevice): Boolean {
        this.noiseFitDevice = noiseFitDevice
        return LocalDataManager.isBind()
    }

    override fun isConnected(): Boolean = BLEManager.isConnected()


    private fun bind() {
        AppLogs.sendAppLogs("Initiate Binding")
        BLEManager.bind()
    }

    private fun unbind() {
        BLEManager.unbind()
    }

    override fun attachCallbacks() {
        removeCallbacks()
        BLEManager.registerConnectCallBack(connectCallBack)
        BLEManager.registerBindCallBack(bindCallBack)
    }

    override fun removeCallbacks() {
        BLEManager.unregisterConnectCallBack(connectCallBack)
        BLEManager.unregisterBindCallBack(bindCallBack)
    }

    private fun onDeviceConnected() {
        noiseFitDevice?.let {
            baseConnectionCallbacks?.onConnect(ConnectState.ConnectSuccess(it,ConnectionEventsConstants.Success))
            baseConnectionCallbacks?.onDeviceReady(noiseFitDevice)
        } ?: LOGS.e("Should Not be null")

    }


    private val unbindCallBack = object : UnbindCallBack.ICallBack {
        override fun onSuccess() {
            isReconnect = false
            BLEManager.disConnect()
            baseConnectionCallbacks?.onConnect(ConnectState.DisconnectSuccess(noiseFitDevice,ConnectionEventsConstants.Disconnect_success))
            AppLogs.sendAppLogs("Disconnect Success")
        }

        override fun onFailed() {}
    }

    private val connectCallBack = object : ConnectCallBack.ICallBack {


        override fun onConnectStart(p0: String?) {
            baseConnectionCallbacks?.onConnect(ConnectState.Connecting(noiseFitDevice,ConnectionEventsConstants.Connecting))
        }

        override fun onConnecting(p0: String?) {
            baseConnectionCallbacks?.onConnect(ConnectState.Connecting(noiseFitDevice,ConnectionEventsConstants.Connecting))
        }

        override fun onRetry(p0: Int, p1: String?) {
            baseConnectionCallbacks?.onConnect(ConnectState.ConnectFailed(noiseFitDevice,ConnectionEventsConstants.Retry))
            AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.Failed.apply { comment = "Retry" })

        }

        override fun onConnectSuccess(p0: String?) {
            baseConnectionCallbacks?.onConnect(ConnectState.DfuMode(noiseFitDevice, 0, false))
            if (isReconnect.not()) {
                bind()

            } else {
                AppLogs.sendAppLogs("Connect Success")
                onDeviceConnected()

            }
        }


        override fun onConnectFailed(p0: ConnectFailedReason?, p1: String) {

            LOGS.d("on connect====CF2===2", "Connect::3 " + p0)

            if (!isReconnect) {
                bindDeviceConnectDeviceCount++
                if (bindDeviceConnectDeviceCount >= bindDeviceConnectDeviceTotalTimes) {
                    if (noiseFitDevice != null) {
                        BLEManager.connect(noiseFitDevice?.let {
                            Colorfit2DeviceConverter.getBleFromColorfit2(
                                it
                            )
                        })
                    }
                } else {
                    baseConnectionCallbacks?.onConnect(ConnectState.ConnectFailed(noiseFitDevice,ConnectionEventsConstants.Failed))
                }

                AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.Failed)
            }


        }

        override fun onConnectBreak(p0: String?) {
            baseConnectionCallbacks?.onConnect(ConnectState.ConnectFailed(noiseFitDevice,ConnectionEventsConstants.Failed))
            AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.Failed)

        }

        override fun onInDfuMode(bleDevice: BLEDevice) {
            WatchInfoGlobals.firmwareDeviceId = bleDevice.mDeviceId
            WatchInfoGlobals.firmwareDeviceAddress = bleDevice.mDeviceAddress
            baseConnectionCallbacks?.onConnect(ConnectState.DfuMode(noiseFitDevice, 0, true))
            AppLogs.sendAppLogs("onInDfuMode")
        }

        override fun onDeviceInNotBindStatus(p0: String?) {

        }

        override fun onInitCompleted(p0: String?) {
            baseConnectionCallbacks?.onInitCompleted(noiseFitDevice)
        }


    }

    override fun startDfuUpdate(fileUri: String) {
        val file1: File = File(Uri.parse(fileUri).path)

        val bleDFUConfig = BleDFUConfig().apply {
            deviceId = "" + WatchInfoGlobals.firmwareDeviceId
            filePath = file1.path
            isNeedReOpenBluetoothSwitchIfFailed = false
            maxRetryTime = 3
            macAddress = WatchInfoGlobals.firmwareDeviceAddress
        }
        BLEManager.removeDFUStateListener(dfuStateListener)
        BLEManager.addDFUStateListener(dfuStateListener)
        BLEManager.startDFU(bleDFUConfig)
    }

    private val dfuStateListener = object : BleDFUState.IListener {
        override fun onPrepare() {
            baseConnectionCallbacks?.onFirmwareUpgradeProgress(
                DeviceFirmware(
                    status = "started",
                    isProgressAvailable = true
                )
            )
        }

        override fun onDeviceInDFUMode() {
            LOGS.d("noise_fit_event:colorfit_pro_2_ota", "firmware_upgrade : in dfu mode")
        }

        override fun onProgress(progress: Int) {
            baseConnectionCallbacks?.onFirmwareUpgradeProgress(
                DeviceFirmware(
                    status = "progress",
                    percentage = progress
                )
            )
            LOGS.d("noise_fit_event:colorfit_pro_2_ota", "firmware_upgrade : progress: $progress")
        }

        override fun onSuccess() {
            baseConnectionCallbacks?.onFirmwareUpgradeProgress(
                DeviceFirmware(
                    status = "success",
                    version = if (LocalDataManager.getBasicInfo()?.firmwareVersion != null) LocalDataManager.getBasicInfo()?.firmwareVersion.toString() else null
                )
            )
            LOGS.d("noise_fit_event:colorfit_pro_2_ota", "firmware_upgrade : success")
        }

        override fun onSuccessAndNeedToPromptUser() {
            baseConnectionCallbacks?.onFirmwareUpgradeProgress(
                DeviceFirmware(
                    status = "success",
                    version = if (LocalDataManager.getBasicInfo()?.firmwareVersion != null) LocalDataManager.getBasicInfo()?.firmwareVersion.toString() else null
                )
            )
            LOGS.d("noise_fit_event:colorfit_pro_2_ota", "firmware_upgrade : successAndPromptUser")
        }

        override fun onFailed(failReason: BleDFUState.FailReason) {
            baseConnectionCallbacks?.onFirmwareUpgradeProgress(
                DeviceFirmware(
                    status = "error",
                    message = failReason.toString()
                )
            )
            LOGS.d("noise_fit_event:colorfit_pro_2_ota", "firmware_upgrade : failed")
        }

        override fun onCanceled() {
            LOGS.d("noise_fit_event:colorfit_pro_2_ota", "firmware_upgrade : cancelled")
        }

        override fun onRetry(count: Int) {
            LOGS.d("noise_fit_event:colorfit_pro_2_ota", "firmware_upgrade : retry : $count")
        }
    }

    private val bindCallBack = object : BindCallBack.ICallBack {
        override fun onSuccess() {
            noiseFitDevice?.let {
                baseConnectionCallbacks?.onConnect(ConnectState.ConnectSuccess(it,ConnectionEventsConstants.Success))
                baseConnectionCallbacks?.onDeviceReady(noiseFitDevice)
                if (noiseFitDevice?.deviceType == DeviceType.COLORFIT_PRO_3.deviceType
                    || noiseFitDevice?.deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType || noiseFitDevice?.deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType
                )
                    setSportModePro3()
            } ?: Log.e("noiseFitDevice", "Should not be null")

        }


        override fun onFailed(bindFailedError: BindCallBack.BindFailedError) {
            baseConnectionCallbacks?.onConnect(ConnectState.ConnectFailed(noiseFitDevice,ConnectionEventsConstants.Failed))
        }

        override fun onCancel() {}

        override fun onReject() {}
        override fun onNeedAuth(p0: Int) {

        }

    }

    private fun setSportModePro3() {
        val functionInfo = LocalDataManager.getSupportFunctionInfo()
        if (functionInfo != null) {
            if (functionInfo.ex_table_main7_v3_sports_type) {
                val sportModeSortV3 = SportModeSortV3()
                //sportModeSortV3.item = getSportInfo()
                //sportModeSortV3.num = getSportInfo().size
                BLEManager.setSportModeSortInfoV3(sportModeSortV3)
            }
        }
    }

}