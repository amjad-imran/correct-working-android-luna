package com.noisefit.hybrid.base

import cn.appscomm.bluetoothsdk.app.BluetoothSDK
import cn.appscomm.ota.util.OtaAppContext
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.interfaces.base.BaseInitializeCallbacks
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.utils.LOGS

class NFHybridApplicationHandler : BaseInitializeInterface() {

    var sdkInitStatus = false

    override fun initSdk() {
        if (!sdkInitStatus) {
            val initStatus = try {
                BluetoothSDK.initSDK(NoisefitApplication.context)
                OtaAppContext.INSTANCE.init(NoisefitApplication.context)
                true
            } catch (exp: ExceptionInInitializerError) {
                exp.printStackTrace()
                false
            } catch (exp: NoClassDefFoundError) {
                exp.printStackTrace()
                false
            } catch (exp: NullPointerException) {
                exp.printStackTrace()
                false
            }
            LOGS.d("init_status $initStatus")

            sdkInitStatus = initStatus
        }

    }

    override fun removeCallback() {

    }

    override fun unInitSdk() {
        BluetoothSDK.endSDK()
        sdkInitStatus = false
    }

    override fun <T> callbackListener(callback: T) {
        callback as BaseInitializeCallbacks
        callback.serviceConnected()
    }
}