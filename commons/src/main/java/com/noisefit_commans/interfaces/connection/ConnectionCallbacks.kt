package com.noisefit_commans.interfaces.connection

import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceFirmware

sealed class ConnectState {
    class Start(val noiseFitDevice: ColorFitDevice?) : ConnectState()
    class Connecting(val noiseFitDevice: ColorFitDevice?, val status: String? = null) :
        ConnectState()

    class DisconnectSuccess(
        val noiseFitDevice: ColorFitDevice?,
        val status: String? = null
    ) : ConnectState()

    class DisconnectFailed(
        val noiseFitDevice: ColorFitDevice?,
        val status: String? = null
    ) : ConnectState()

    class ConnectSuccess(val noiseFitDevice: ColorFitDevice, val status: String? = null) :
        ConnectState()

    class ConnectFailed(val noiseFitDevice: ColorFitDevice?, val status: String? = null) :
        ConnectState()

    class DfuMode(
        val noiseFitDevice: ColorFitDevice?,
        val version: Int,
        val needForceOTA: Boolean
    ) : ConnectState()

    class UnPaired() : ConnectState()
    class ReconnectStatus(val watchBindState:WatchBindState) : ConnectState()
}

sealed class BindState {
    object BindSuccess : BindState()
    object BindFailure : BindState()
    object UnbindSuccess : BindState()
    object UnbindFailure : BindState()
}

sealed class WatchBindState {
    object InvalidToken : WatchBindState()
    object WatchIsUnbind : WatchBindState()

    object AlreadyPaired : WatchBindState()
}


interface ConnectionCallbacks {
    fun onConnect(connectState: ConnectState)
    fun onBluetoothConnect(isBluetoothConnected: Boolean)
    fun onInitCompleted(noiseFitDevice: ColorFitDevice?)
    fun onBind(noiseFitDevice: ColorFitDevice?, bindState: BindState)
    fun onDeviceReady(noiseFitDevice: ColorFitDevice?)
    fun onFirmwareUpgradeProgress(firmware: DeviceFirmware)
}