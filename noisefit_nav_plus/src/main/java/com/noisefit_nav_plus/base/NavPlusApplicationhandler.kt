package com.noisefit_nav_plus.base

import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.interfaces.base.BaseInitializeCallbacks
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.utils.LOGS
import com.zjw.zhbraceletsdk.linstener.ZHInitStatusListener
import com.zjw.zhbraceletsdk.service.ZhBraceletService
import com.zjw.zhbraceletsdk.service.ZhServiceHolder
import javax.inject.Inject

class NavPlusApplicationHandler
@Inject
constructor() : BaseInitializeInterface() {
    private val TAG = "NavPlusApplicationHandler"
    private var baseInitializeCallbacks: BaseInitializeCallbacks? = null

    private var isInitialized = false

    override fun <T> callbackListener(callback: T) {
        baseInitializeCallbacks = callback as BaseInitializeCallbacks
    }

    override fun removeCallback() {
        baseInitializeCallbacks = null
    }

    fun getZhBraceletService(): ZhBraceletService? {
        return ZhServiceHolder.getInstance().getService()
    }

    fun openBleService(listener: ZHInitStatusListener?) {
        LOGS.i(TAG, "openBleService")
        if (!isInitialized) {
            isInitialized = true
            ZhServiceHolder.getInstance()
                .initSDK(NoisefitApplication.context, object : ZHInitStatusListener {
                    override fun onInitComplete() {
                        LOGS.i(TAG, "ZH SDK onInitComplete")
                        listener?.onInitComplete()
                        baseInitializeCallbacks?.serviceConnected()
                    }
                })
        }
    }

    override fun initSdk() {
        LOGS.i("initSdk")
        openBleService(null)
    }

    override fun unInitSdk() {
        LOGS.i("unInitSdk")
        if (isInitialized) {
            ZhServiceHolder.getInstance().release()
            baseInitializeCallbacks?.serviceDisconnected()
            isInitialized = false
        }
    }

}