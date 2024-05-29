package com.noisefit_zhsdk.log

import android.content.Context
import android.text.TextUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Created by Android on 2023/7/4.
 *
 */
class ZhLogger(mContext: Context) {
    //日志文件
    private var logFile: File? = null

    //日志路径文件夹
    private var fileDir = ""

    //日志路径
    private var filePath = ""

    //文件名前缀
    private var prefixFlag = "LOG"

    //文件名后缀
    private var suffixFlag = "log"

    //文件名
    private var fileName = prefixFlag

    //是否允许写入文件
    private var isWriteLog = true

    //清除日志时间天数
    private var expiredDay = 10

    init {
        fileName = "log"
        fileDir = mContext.getExternalFilesDir("log")?.absolutePath ?: ""
    }

    /**
     * 设置文件路径
     */
    fun setFileDirPath(dir: String?) {
        if (TextUtils.isEmpty(dir)) return
        val dirFile = File(dir!!)
        if (if (dirFile.exists()) dirFile.isDirectory else dirFile.mkdirs()) {
            fileDir = dir
        }
    }

    fun setPrefixFlag(flag: String?) {
        if (flag.isNullOrEmpty()) return
        prefixFlag = flag
    }

    /**
     * 设置是否允许写入文件
     */
    fun setIsWriteLog(isWriteLog: Boolean) {
        this.isWriteLog = isWriteLog
    }

    /**
     * 设置过期清除天数
     *
     * @param expiredDay
     */
    fun setExpiredDay(expiredDay: Int) {
        this.expiredDay = expiredDay
    }

    /**
     * 获取日志文件路径
     */
    fun getLogFilePath(): String {
        if (filePath.isEmpty()) createFile()
        return filePath
    }

    fun setSuffixFlag(flag: String?) {
        if (flag.isNullOrEmpty()) return
        suffixFlag = flag
    }

    /**
     * 创建文件
     */
    private fun createFile() {
        fileName = createFileName()
        try {
            logFile = File(File(fileDir), "$fileName.$suffixFlag")
            filePath = logFile!!.absolutePath
            if (!logFile!!.exists()) {
                logFile!!.createNewFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    private fun createFileName(): String {
        return prefixFlag + "_" + dateFormat.format(Date())
    }

    /**
     * 记录日志文件，用于测试版本apk记录日志
     *
     * @param tag
     * @param msg
     * @param isMainStyle 主风格
     */
    fun writeFile(tag: String?, msg: String?, append: Boolean = true, isMainStyle: Boolean = true) {
        if (!isWriteLog) return
        createFile()
        try {
            if (isMainStyle) {
                val mTag = if (TextUtils.isEmpty(tag)) "" else tag!!.lowercase(Locale.getDefault())
                val buffer = StringBuffer()
                buffer.append(logTime())
                buffer.append(" ----> ")
                buffer.append(mTag)
                buffer.append(" ")
                for (i in mTag.length..24) {
                    buffer.append("-")
                }
                buffer.append("> ")
                buffer.append(msg)
                buffer.append("\r\n")
                FileIOUtils.writeFileFromString(logFile, buffer.toString(), append)
            } else {
                val buffer = StringBuffer()
                if (msg != null) buffer.append(msg)
                FileIOUtils.writeFileFromString(logFile, buffer.toString(), append)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val logTimeDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.ENGLISH)

    private fun logTime(): String? {
        return logTimeDateFormat.format(Date())
    }

    /**
     * 读取当前日志内容
     */
    fun readLogContent(): String? {
        createFile()
        return  FileIOUtils.readFile2String(logFile)
    }

    //region 清缓存
    /**
     * 清除过期文件
     * 保留expiredDay天前的日志
     */
    fun clearExpiredFile() {
        try {
            val logDir = File(fileDir)
            val logFiles = FileUtils.listFilesInDirWithFilter(logDir, { pathname -> //取文件夹内所有txt 文件
                pathname != null && (pathname.absolutePath.endsWith("txt") || pathname.absolutePath.endsWith(
                    suffixFlag
                ))
            }, false)
            //Log.d("Logger", "$prefixFlag logFiles:${logFiles?.size}")
            if (logFiles != null) {
                for (i in logFiles.indices) {
                    val fileNameDate = FileUtils.getFileNameNoExtension(logFiles[i].absolutePath).replace(
                        prefixFlag + "_",
                        ""
                    )
                    val fileTime = dateFormat.parse(fileNameDate)?.time ?: 0L
                    val expired =
                        abs(System.currentTimeMillis() - fileTime) > expiredDay * 24 * 60 * 60 * 1000L
                    if (expired) {
                        val isDel = logFiles[i].isFile && logFiles[i].delete()
                        //Log.d("Logger", logFiles[i].toString() + " expired， delete:" + isDel)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //endregion

    //region 清所有日志
    /**
     * 清所有日志
     */
    fun clearLog() {
        try {
            FileUtils.deleteFilesInDir(File(fileDir))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    //endregion

}