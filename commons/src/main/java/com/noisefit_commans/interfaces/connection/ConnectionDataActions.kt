package com.noisefit_commans.interfaces.connection

import com.noisefit_commans.interfaces.base.BaseActions
import com.noisefit_commans.models.ColorFitDevice

abstract class ConnectionDataActions() :
    BaseActions() {

    open fun onConnectedQRBinding() {}
    abstract fun connect(noiseFitDevice: ColorFitDevice)
    abstract fun disconnect(noiseFitDevice: ColorFitDevice)
    abstract fun forceDisconnect(noiseFitDevice: ColorFitDevice)
    abstract fun reconnect(noiseFitDevice: ColorFitDevice,type: Boolean)
    abstract fun isDevicePaired(noiseFitDevice: ColorFitDevice): Boolean
    abstract fun isConnected(): Boolean
    open fun getConnectionTimerDelay(): Long? = null
    open fun disconnectFromService(){}
    open fun checkWatchBindStatus() {}
    open fun startDfuUpdate(fileUri : String) {}
}