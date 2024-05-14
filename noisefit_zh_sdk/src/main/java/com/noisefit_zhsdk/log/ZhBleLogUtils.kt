package com.noisefit_zhsdk.log

import android.content.Context
import android.net.Uri
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.UriUtils
import com.blankj.utilcode.util.ZipUtils
import com.noisefit_commans.NoisefitApplication
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.coroutines.resume

object ZhBleLogUtils {
    private var isRelease = false

    //蓝牙日志
    private var bleZhLogger: ZhLogger? = null
    private const val ZH_BLE_LOG_DIR_NAME = "ble"
    private const val ZH_BLE_LOG_PREFIX_NAME = "BLE"
    private const val ZH_BLE_LOG_SUFFIX_NAME = "log"

    /**
     * 初始化
     */
    fun initLogger(context: Context, isWriteLog: Boolean, isRelease: Boolean) {
        this.isRelease = isRelease

        bleZhLogger = ZhLoggerBuilder(context)
            .setIsWriteLog(isWriteLog)
            .setExpiredDay(7)
            .setFileDirPath(getDirPath(context, isRelease, ZH_BLE_LOG_DIR_NAME))
            .setPrefixFlag(ZH_BLE_LOG_PREFIX_NAME)
            .setSuffixFlag(ZH_BLE_LOG_SUFFIX_NAME)
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
        return bleZhLogger?.getLogFilePath() ?: ""
    }

    @JvmStatic
    fun bleLog(tag: String?, msg: String?) {
        bleZhLogger?.writeFile(tag, msg)
    }
    //endregion

    suspend fun getUriByBleAllLog(): Uri? {
        return withTimeoutOrNull(30 * 1000) {
            suspendCancellableCoroutine<Uri?> {
                val dir = getDirPath(
                    NoisefitApplication.context!!.applicationContext,
                    isRelease,
                    ZH_BLE_LOG_DIR_NAME
                )
                val zipFilePath = dir + File.separator + "Ring_log_${
                    TimeUtils.getNowString(
                        TimeUtils.getSafeDateFormat("yyyy-MM-dd")
                    )
                }.zip"
                deleteAllZipInDir(dir)
                FileUtils.createFileByDeleteOldFile(zipFilePath)
                val files = FileUtils.listFilesInDirWithFilter(
                    dir, { pathname -> //取文件夹内所有文件
                        pathname != null && pathname.absolutePath.endsWith(ZH_BLE_LOG_SUFFIX_NAME)
                    }, false
                )
                try {
                    val filePaths = mutableListOf<String>()
                    for (f in files) {
                        filePaths.add(f.absolutePath)
                    }
                    ZipUtils.zipFiles(filePaths, zipFilePath)
                    it.resume(UriUtils.file2Uri(FileUtils.getFileByPath(zipFilePath)))
                } catch (e: Exception) {
                    it.resume(null)
                }
            }
        }
    }

    fun deleteAllZipInDir(dir:String) {
        FileUtils.deleteFilesInDirWithFilter(dir) { pathname ->
            pathname != null && pathname.absolutePath.endsWith("zip")
        }
    }

}