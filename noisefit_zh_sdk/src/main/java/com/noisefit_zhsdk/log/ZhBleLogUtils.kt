package com.noisefit_zhsdk.log

import android.content.Context
import java.io.File

object ZhBleLogUtils {


    //蓝牙日志
    private var bleLogger: Logger? = null

    fun initLogger(context: Context, isWriteLog: Boolean, isRelease: Boolean) {
        bleLogger = LoggerBuilder(context)
            .setIsWriteLog(isWriteLog)
            .setExpiredDay(7)
            .setFileDirPath(getDirPath(context, isRelease, "ble"))
            .setPrefixFlag("BLE")
            .build()

    }

    /**
     * 获取文件存储路径
     */
    private fun getDirPath(context: Context, isRelease: Boolean, key: String): String {
        return if (isRelease) {
            context.filesDir.absolutePath + File.separator + "log" + File.separator + key
        } else {
            context.getExternalFilesDir("log" + File.separator + key)?.absolutePath ?: ""
        }
    }


    //region 蓝牙日志
    fun bleLogPath(): String {
        return bleLogger?.getLogFilePath() ?: ""
    }

    @JvmStatic
    fun bleLog(tag: String?, msg: String?) {
        bleLogger?.writeFile(tag, msg)
    }
    //endregion

}