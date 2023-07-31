package com.noisefit.colorfit_pro.handler.connect

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.crrepa.ble.conn.CRPBleConnection
import com.crrepa.ble.conn.CRPBleDevice
import com.crrepa.ble.conn.listener.CRPBleConnectionStateListener
import com.noisefit.colorfit_pro.base.ProApplicationHandler
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.ConnectionEventsConstants
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.connection.ConnectionCallbacks
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.utils.LOGS
import java.lang.reflect.Method
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

class ProConnectHandler
@Inject
constructor(private val proApplicationHandler: ProApplicationHandler) : ConnectionDataActions() {

    private val TAG = ProConnectHandler::class.java.simpleName

    companion object {
        var bleConnection: CRPBleConnection? = null
    }

    private var baseConnectionCallbacks: ConnectionCallbacks? = null
    private var noiseFitDevice: ColorFitDevice? = null
    private val RECONNECTION_DELAY: Long = 3000
    private var connecting = false
    private var connected = false
    private var isReconnect = false

    private var isDisconnectClicked = false


    override fun disconnect(noiseFitDevice: ColorFitDevice) {
        LOGS.d(TAG, "on Disconnect")
        isDisconnectClicked = true
        proApplicationHandler.getBleDevice(noiseFitDevice)?.let {
            LOGS.d(TAG, "on Disconnected")
            disconnect(it)
        }
    }


    override fun getConnectionTimerDelay(): Long? = 10000L


    override fun reconnect(noiseFitDevice: ColorFitDevice, type: Boolean) {
//        if (CommonGlobals.isWatchDataUpdating) {
//            return
//        }
        this.noiseFitDevice = noiseFitDevice
        isReconnect = type
        LOGS.d(TAG, "Qube reconnect")
//        this.isReconnectFromReact = type
//        if (isConnected() && !CommonGlobals.isCloudWatchFaceUpdating) {
//           LOGS.d( "watch has been reconnected")
////            handleConnected()
//            return
//        }
        closeGatt()
        setConnecting(false)
        delayConnect()
    }

    override fun isDevicePaired(noiseFitDevice: ColorFitDevice): Boolean {
        this.noiseFitDevice = noiseFitDevice
        return true
    }

    private fun isBluetoothEnable(): Boolean {
        return proApplicationHandler.getBleClient().isBluetoothEnable
    }

    private fun closeGatt() {
        bleConnection?.let {
            LOGS.d(TAG, "closeGatt")
            bleConnection?.close()
            proApplicationHandler.reset()
            bleConnection = null
        }

    }

    @SuppressLint("CheckResult")
    private fun delayConnect() {
        Timer("DelayConnection", false).schedule(RECONNECTION_DELAY) {
            noiseFitDevice?.let {
                connect(it)
            }
        }


    }

    override fun isConnected(): Boolean {
        if (proApplicationHandler.getBleDevice(noiseFitDevice) == null) {
            return false
        }
        if (bleConnection == null) {
            return false
        }
        return if (proApplicationHandler.getBleDevice(noiseFitDevice)?.isConnected == false) {
            false
        } else connected


    }


    private fun disconnect(bleDevice: CRPBleDevice?) {
        isDisconnectClicked = true
        isReconnect = false
        proApplicationHandler.reset()
        bleConnection?.reset()
        setConnecting(false)
        bleDevice?.disconnect()
        baseConnectionCallbacks?.onConnect(
            ConnectState.DisconnectSuccess(
                noiseFitDevice,
                ConnectionEventsConstants.Disconnect_success
            )
        )
        //checkForDisconnectState()
    }


    private fun btBindStatus() {
        bleConnection?.queryBtAddress {
            val device = proApplicationHandler.getBleClient()
                .getBleDevice(it)?.bluetoothDevice
            //device?.javaClass?.getMethod("createBond", Int.javaClass)
            if (ActivityCompat.checkSelfPermission(
                    NoisefitApplication.context!!,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                LOGS.d(TAG, "Qube not btBindStatus")
                return@queryBtAddress
            }
            try {
                if (device?.bondState == BluetoothDevice.BOND_NONE) {
                    device.createBond()
                    LOGS.d(TAG, "Qube btBindStatus")
                }
                LOGS.d(TAG, "Qube already btBindStatus")


            } catch (e: Exception) {
                LOGS.e(e, TAG, "Qube btBindStatus exception")
                e.printStackTrace()
            }

        }
    }


    //   private fun removeBond() {
//        /*
//       * There is a removeBond() method in BluetoothDevice class but for now it's hidden.
//       * We will call it using reflections.
//       */
//        if (ActivityCompat.checkSelfPermission(
//                NoisefitApplication.context!!,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            LOGS.d("$TAG  Qube no permission")
//
//            isDisconnectClicked = true
//            connected = false
//            isReconnect = false
//            proApplicationHandler.reset()
//            bleConnection?.reset()
//            setConnecting(false)
////            bleDevice?.disconnect()
//            checkForDisconnectState()
//            return
//        }
////       val device = proApplicationHandler.getBleClient()
////           .getBleDevice(it)?.bluetoothDevice
//
//
//        bleConnection?.queryBtAddress {
//            val device = proApplicationHandler.getBleClient()
//                .getBleDevice(it)?.bluetoothDevice
//            if(device == null){
//                LOGS.d( "$TAG device is null")
//            }
//            LOGS.d( "$TAG device is null ----> ${device?.bondState}")
//            try {
//                if (device?.bondState != BluetoothDevice.BOND_NONE) {
//                val removeBond: Method? = device?.javaClass?.getMethod("removeBond")
//                LOGS.d( "$TAG device.removeBond() (hidden)")
//                removeBond?.invoke(device)
//                if(removeBond == null){
//                    LOGS.d( "$TAG removeBond is null")
//                }
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                LOGS.e(e, TAG, "An exception occurred while removing bond")
//            } finally {
//                isDisconnectClicked = true
//                connected = false
//                isReconnect = false
//
//                bleConnection?.reset()
//                setConnecting(false)
//                bleDevice?.disconnect()
//                checkForDisconnectState()
//                proApplicationHandler.reset()
//            }
//        }
//
//
//    }
    private fun checkForDisconnectState() {
        LOGS.d(TAG, "Qube checkForDisconnectState")
        Timer("checkForDisconnectState", false).schedule(RECONNECTION_DELAY) {
            if (!isConnected()) {
                baseConnectionCallbacks?.onConnect(
                    ConnectState.DisconnectSuccess(
                        noiseFitDevice,
                        ConnectionEventsConstants.Disconnect_success
                    )
                )
            } else {
                baseConnectionCallbacks?.onConnect(
                    ConnectState.DisconnectFailed(
                        noiseFitDevice,
                        ConnectionEventsConstants.Failed
                    )
                )
            }
        }
    }


    override fun connect(noiseFitDevice: ColorFitDevice) {
        this.noiseFitDevice = noiseFitDevice
        /* if (!isBluetoothEnable()) {
             LOGS.d(TAG, "Qube bluetooth disabled ")
             return
         }*/
        if (!canConnect()) {
            LOGS.d(TAG, "Qube is already connected")
            return
        }
        setConnecting(true)

        LOGS.d(TAG, "Qube Connection started")
        bleConnection = proApplicationHandler.getBleDevice(noiseFitDevice)?.connect()
        bleConnection?.setConnectionStateListener(this.bleConnectionStateListener)

    }

    private fun canConnect(): Boolean {


        if (isConnected()) {
            LOGS.d(TAG, "Qube isConnected ")
            return false
        }
        if (isConnecting()) {
            LOGS.d(TAG, "Qube isConnecting progress")
            return false
        }


        return true
    }

    private fun handleConnected() {
        setConnecting(false)
//        if (CommonGlobals.isWatchDataUpdating.not()) {
        LOGS.d("$TAG handleConnected : ${isReconnect}")
        if (!isReconnect) {
            btBindStatus()
        }

        noiseFitDevice?.let {
            baseConnectionCallbacks?.onConnect(
                ConnectState.ConnectSuccess(
                    it,
                    ConnectionEventsConstants.Success
                )
            )
            baseConnectionCallbacks?.onDeviceReady(it)
        }


//            CFProQueryDeviceDataHandler.attachCallbacks()
//            CFProUpdateDeviceDataHandler.attachCallbacks()

//        }
    }

    private fun clearConnectState() {
        setConnecting(false)
    }

    private fun isConnecting(): Boolean {
        return connecting
    }

    override fun disconnectFromService() {
        proApplicationHandler.getBleDevice(noiseFitDevice)?.disconnect()
    }

    private fun setConnecting(connecting: Boolean) {
        this.connecting = connecting
    }

    private val bleConnectionStateListener: CRPBleConnectionStateListener =
        CRPBleConnectionStateListener {
            when (it) {
                CRPBleConnectionStateListener.STATE_CONNECTED -> {
                    this.connected = true

                    LOGS.d(
                        TAG, "connection status : STATE_CONNECTED"
                    )
                    handleConnected()

                }

                CRPBleConnectionStateListener.STATE_DISCONNECTED -> {
                    clearConnectState()
                    LOGS.d(
                        TAG, "connection status : STATE_DISCONNECTED"
                    )

                    this.connected = false
                    if (isDisconnectClicked) {

                        baseConnectionCallbacks?.onConnect(
                            ConnectState.DisconnectSuccess(
                                noiseFitDevice,
                                ConnectionEventsConstants.Disconnect_success
                            )
                        )
                        isDisconnectClicked = false

                    } else {
                        if (proApplicationHandler.getBleDeviceNullable() != null) {
                            baseConnectionCallbacks?.onConnect(
                                ConnectState.ConnectFailed(
                                    noiseFitDevice,
                                    ConnectionEventsConstants.Failed
                                )
                            )
                        }
                    }
                }
                else -> {
                    this.connected = false
                }
            }
        }

    override fun <T> callbackListener(callback: T) {
        baseConnectionCallbacks = callback as ConnectionCallbacks
    }


    override fun <T> callbackListenerNew(callback: T) {

    }

}