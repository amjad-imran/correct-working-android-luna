package com.noisefit.colorfit_pro.base

import android.content.Context
import com.crrepa.ble.CRPBleClient
import com.crrepa.ble.conn.CRPBleDevice
import com.noisefit_commans.interfaces.base.BaseInitializeCallbacks
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.models.ColorFitDevice
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

class ProApplicationHandler
@Inject
constructor(val appContext: Context) : BaseInitializeInterface() {
    private var crpBleClient: CRPBleClient? = null
    private var baseInitializeCallbacks: BaseInitializeCallbacks? = null

    private var bleDevice: CRPBleDevice? = null
    fun getBleDevice(noiseFitDevice: ColorFitDevice?): CRPBleDevice? {
        if (bleDevice == null) {
            bleDevice = getBleClient()
                .getBleDevice(noiseFitDevice?.address)
        }
        return bleDevice
    }

    override fun removeCallback() {
        baseInitializeCallbacks = null
    }

    fun reset(){
        bleDevice = null
    }

    fun getBleDeviceNullable():CRPBleDevice?{
        return bleDevice
    }

    override fun initSdk() {
        Timer("DelayConnection", false)
            .schedule(3000) {
                getBleClient()
                baseInitializeCallbacks?.serviceConnected()
            }
    }

    override fun unInitSdk() {
        bleDevice = null
        crpBleClient = null
        baseInitializeCallbacks?.serviceDisconnected()
    }

    fun getBleClient(): CRPBleClient {
        if (crpBleClient == null) {
            crpBleClient = CRPBleClient.create(appContext)
        }
        return crpBleClient!!
    }

    override fun <T> callbackListener(callback: T) {
        baseInitializeCallbacks = callback as BaseInitializeCallbacks

    }

}

