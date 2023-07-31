package com.noisefit_cf2.base

import com.ido.ble.BLEManager
import com.ido.ble.InitParam
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.base.BaseInitializeCallbacks
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.FileLogsUtils
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject

class ColorFit2ApplicationHandler
@Inject
constructor(
    private var watchDataStore: WatchDataStore
) : BaseInitializeInterface() {

    private var sdkInitStatus = false

    override fun initSdk() {
        if (!sdkInitStatus) {
            LOGS.i("CF2", "initSdk")
            BLEManager.onApplicationCreate(NoisefitApplication.context)
            val fileName = "${DateFormats.getLogDateFormatForColorPro()}.log"
            watchDataStore.saveLogPathName(fileName)
            /*val path = FileLogsUtils.getFolderPath(NoisefitApplication.context!!)
            val initParam = InitParam()
            initParam.isEnableLog = true
            initParam.log_save_days = 1
            initParam.log_save_path = path.toString()
            initParam.soJinLogSavePath = path.toString()
            BLEManager.init(initParam)*/
            BLEManager.init()
            sdkInitStatus = true
        }
    }

    override fun removeCallback() {

    }

    override fun unInitSdk() {
        sdkInitStatus = false
    }

    override fun <T> callbackListener(callback: T) {
        callback as BaseInitializeCallbacks
        callback.serviceConnected()
    }
}