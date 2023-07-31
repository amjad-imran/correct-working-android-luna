package com.noisefit_evolve2.base

import android.content.Context
import android.content.Intent
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.interfaces.base.BaseInitializeCallbacks
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.utils.LOGS
import com.touchgui.sdk.TGBleClient
import com.touchgui.sdk.TGClient
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule

class Evolve2ApplicationHandler
@Inject
constructor(private val context: Context) : BaseInitializeInterface() {

    private var baseInitializeCallbacks: BaseInitializeCallbacks? = null

    override fun <T> callbackListener(callback: T) {
        baseInitializeCallbacks = callback as BaseInitializeCallbacks
    }

    override fun removeCallback() {
        baseInitializeCallbacks = null
    }

    private var mClient: TGClient? = null

    fun getTGBleClient(): TGClient? {
        if (mClient == null) {
            TGBleClient.init(true) { p0, p1 ->
                LOGS.d("LOGS_NEW ${p0}  ${p1}")
            }
            mClient = TGBleClient.Builder(context)
                .disableAutoReconnect()
                .build()
        }

        return mClient
    }

    private fun getServiceIntent(): Intent {
        return Intent(NoisefitApplication.context, TGBleClient::class.java)
    }

    fun openBleService(): TGClient? {
        return getTGBleClient()
    }


    override fun initSdk() {
        LOGS.i("initSdk Evolve2ApplicationHandler")
        openBleService()
        Timer("DelayConnection", false)
            .schedule(3000) {
                //TGLogger.setDebug(true)
                baseInitializeCallbacks?.serviceConnected()
            }

    }

    override fun unInitSdk() {
        mClient?.disconnect()
        baseInitializeCallbacks?.serviceDisconnected()
    }


}