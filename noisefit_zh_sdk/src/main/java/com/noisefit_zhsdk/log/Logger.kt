package com.noisefit_zhsdk.log

import android.content.Context
import android.text.TextUtils
import java.io.BufferedInputStream
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Created by Android on 2023/7/4.
 *
 */
class Logger(mContext: Context) {
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
                writeFileFromString(logFile, buffer.toString(), append)
            } else {
                val buffer = StringBuffer()
                if (msg != null) buffer.append(msg)
                writeFileFromString(logFile, buffer.toString(), append)
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
        return readFile2String(logFile)
    }

    /**
     * 返回文件中的字符串。
     */
    fun readFile2String(file: File?, charsetName: String? = null): String? {
        val bytes: ByteArray = readFile2BytesByStream(file)
            ?: return null
        return if (TextUtils.isEmpty(charsetName)) {
            String(bytes)
        } else {
            try {
                String(bytes, charset(charsetName!!))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                ""
            }
        }
    }

    private val sBufferSize = 524288

    /**
     * 按流返回文件中的字节。
     */
    private fun readFile2BytesByStream(
        file: File?
    ): ByteArray? {
        return if (file == null || !file.exists()) null else try {
            var os: ByteArrayOutputStream? = null
            val `is`: InputStream = BufferedInputStream(
                FileInputStream(file),
                sBufferSize
            )
            try {
                os = ByteArrayOutputStream()
                val b = ByteArray(sBufferSize)
                var len: Int
                while (`is`.read(b, 0, sBufferSize).also {
                        len = it
                    } != -1) {
                    os.write(b, 0, len)
                }

                os.toByteArray()
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    os?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 字符串内容写入文件
     */
    fun writeFileFromString(
        file: File?,
        content: String?,
        append: Boolean
    ): Boolean {
        if (file == null || content == null) return false
        if (!file.isFile || !file.exists()) {
            return false
        }
        var bw: BufferedWriter? = null
        return try {
            bw = BufferedWriter(FileWriter(file, append))
            bw.write(content)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            try {
                bw?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    //region 清缓存
    /**
     * 清除过期文件
     * 保留expiredDay天前的日志
     */
    fun clearExpiredFile() {
        try {
            val logDir = File(fileDir)
            val logFiles = listFilesInDirWithFilterInner(logDir, { pathname -> //取文件夹内所有zh txt 文件
                pathname != null && (pathname.absolutePath.endsWith("txt") || pathname.absolutePath.endsWith(
                    "zh"
                ))
            }, false)
            //Log.d("Logger", "$prefixFlag logFiles:${logFiles?.size}")
            if (logFiles != null) {
                for (i in logFiles.indices) {
                    val fileNameDate = getFileNameNoExtension(logFiles[i].absolutePath).replace(
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

    private fun listFilesInDirWithFilterInner(
        dir: File?,
        filter: FileFilter,
        isRecursive: Boolean
    ): List<File>? {
        try {
            val list: MutableList<File> = ArrayList()
            if (!(dir != null && dir.exists() && dir.isDirectory)) return list
            val files = dir.listFiles()
            if (files != null && files.isNotEmpty()) {
                for (file in files) {
                    if (filter.accept(file)) {
                        list.add(file)
                    }
                    if (isRecursive && file.isDirectory) {
                        val subList = listFilesInDirWithFilterInner(file, filter, true)
                        if (subList != null) {
                            list.addAll(subList)
                        }
                    }
                }
            }
            return list
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getFileNameNoExtension(filePath: String): String {
        try {
            if (TextUtils.isEmpty(filePath)) return ""
            val lastPoi = filePath.lastIndexOf('.')
            val lastSep = filePath.lastIndexOf(File.separator)
            if (lastSep == -1) {
                return if (lastPoi == -1) filePath else filePath.substring(0, lastPoi)
            }
            return if (lastPoi == -1 || lastSep > lastPoi) {
                filePath.substring(lastSep + 1)
            } else filePath.substring(lastSep + 1, lastPoi)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
    //endregion

    //region 清所有日志
    /**
     * 清所有日志
     */
    fun clearLog() {
        try {
            deleteFilesInDir(File(fileDir))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteFilesInDir(dir: File?): Boolean {
        try {
            if (dir == null) return false
            // dir doesn't exist then return true
            if (!dir.exists()) return true
            // dir isn't a directory then return false
            if (!dir.isDirectory) return false
            val files = dir.listFiles()
            if (files != null && files.size != 0) {
                for (file in files) {
                    if (file.isFile) {
                        if (!file.delete()) return false
                    } else if (file.isDirectory) {
                        if (!deleteDir(file)) return false
                    }
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun deleteDir(dir: File?): Boolean {
        try {
            if (dir == null) return false
            if (!dir.exists()) return true
            if (!dir.isDirectory) return false
            val files = dir.listFiles()
            if (files != null && files.isNotEmpty()) {
                for (file in files) {
                    if (file.isFile) {
                        if (!file.delete()) return false
                    } else if (file.isDirectory) {
                        if (!deleteDir(file)) return false
                    }
                }
            }
            return dir.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
    //endregion

}