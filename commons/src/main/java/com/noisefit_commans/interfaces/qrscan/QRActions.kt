package com.noisefit_commans.interfaces.qrscan


import com.noisefit_commans.interfaces.base.BaseActions
import com.noisefit_commans.models.ColorFitDevice

abstract class QRActions(var baseConnectionCallbacks: QRCallbacks?) : BaseActions() {
    abstract fun connect(noiseFitDevice: ColorFitDevice)
    abstract fun disconnect(noiseFitDevice: ColorFitDevice)
    abstract fun reconnect(type: Boolean)
    abstract fun isDevicePaired(noiseFitDevice: ColorFitDevice): Boolean
    abstract fun isConnected(): Boolean
    open fun getConnectionTimerDelay(): Long? = null
}