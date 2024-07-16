package com.noisefit_commans.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.ClassicFlattener
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.Printer
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy
import com.elvishew.xlog.printer.file.naming.FileNameGenerator
import com.noisefit_commans.BuildConfig
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Locale


private const val LogsFolder = "appLogs"
private const val LogsTxtFile = "logs.txt"


object AppLogs {
    private var filePrinter: Printer? = null
    private val TAG = "AppLogs"
    const val FILE_PROVIDER = "com.noisefit.luna.fileprovider"

    private val scope = CoroutineScope(Dispatchers.IO)
    private val backgroundDispatcher = newFixedThreadPoolContext(1, "File Write-App")

    /**
     * Print App Logs, Exceptions
     * @param event Event Type
     * @param subEvent Sub Events
     */
    fun sendAppLogs(event: LogEvents, subEvent: SubEvent) {
        scope.launch(backgroundDispatcher) {
            cleanLogFilesIfNecessary()
            LOGS.d(TAG, "Saving Event :  ${String.format(locale = Locale.US,"%02X %02X", event.code, subEvent.code)}")
            val exception = "Exception = ${
                String.format(
                    locale = Locale.US,
                    "%02X %02X",
                    event.code,
                    subEvent.code
                )
            } ${if (!subEvent.comment.isNullOrEmpty()) "\nComment : ${subEvent.comment}" else ""}"
            XLog.printers(filePrinter).e(exception)
        }

    }

    /**
     * Print app Logs text
     */
    fun sendAppLogs(logText: String) {
        scope.launch(backgroundDispatcher) {
            cleanLogFilesIfNecessary()
            XLog.printers(filePrinter).i(logText)
        }
    }

    /**
     * Print Debug app Logs text
     */
    fun sendDebugAppLogs(logText: String) {
        if (!BuildConfig.DEBUG) return

        scope.launch(backgroundDispatcher) {
            cleanLogFilesIfNecessary()
            XLog.printers(filePrinter).i(logText)
        }
    }

    fun initAppLogs(appContext: Context) {

        val androidPrinter = AndroidPrinter()
        val filePrinter =
            FilePrinter
                .Builder(File(appContext.externalCacheDir?.absolutePath, LogsFolder).path)
                .fileNameGenerator(object : FileNameGenerator {
                    override fun isFileNameChangeable(): Boolean {
                        return false
                    }

                    override fun generateFileName(logLevel: Int, timestamp: Long): String {
                        return LogsTxtFile
                    }
                })
                .flattener(ClassicFlattener())
                .backupStrategy(NeverBackupStrategy())
                .build()

        this.filePrinter = filePrinter

        XLog.init(androidPrinter, filePrinter)

    }

    private fun cleanLogFilesIfNecessary() {
        NoisefitApplication.context?.applicationContext?.let {
            val logDir: File = getFolderPath(it)
            val files = logDir.listFiles() ?: return
            for (file in files) {
                if (file.sizeInMb > 2) {
                    clearNLines(file, 2000)
                    //file.delete()
                }
            }
        }

    }

    private fun clearNLines(logFile: File, lines: Int) {

        tryCatch {

            val reader = BufferedReader(FileReader(logFile))
            val stringBuilder = StringBuilder()
            var line: String?

            for (i in 0 until lines) {
                reader.readLine()
            }

            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
            reader.close()

            val writer = FileWriter(logFile)
            writer.write(stringBuilder.toString())
            writer.flush()
            writer.close()

        }
    }

    fun getLogsFolder(): String {
        return LogsFolder
    }

    fun deleteFile() {
        NoisefitApplication.context?.applicationContext?.let {
            val logDir: File = getFolderPath(it)
            val files = logDir.listFiles() ?: return
            for (file in files) {
                file.delete()
            }
        }
    }

    fun getFileUri(
        context: Context
    ): Uri? {
        val path = "$LogsFolder/$LogsTxtFile"
        return getUri(path, context)
    }

    suspend fun getFile(context: Context): File? {
        val path = "$LogsFolder/$LogsTxtFile"
        return getFile(path, context)
    }

    private fun getUri(path: String, context: Context): Uri? {
        val file = getFile(path, context)
        if (!file.exists()) {
            return null
        }
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.let {
                    return FileProvider.getUriForFile(
                        it,
                        FILE_PROVIDER,
                        file
                    )
                }
            } else {
                Uri.fromFile(file)
            }
        } catch (exp: Exception) {
            return null
        }
    }

    fun getFolderPath(context: Context): File {
        val logFilePath = LogsFolder
        return getFile(logFilePath, context)
    }

    private fun getFile(logFilePath: String, context: Context): File {
        val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            File(context.externalCacheDir?.absolutePath, logFilePath)
        } else {
            File(Environment.getExternalStorageDirectory().toString(), logFilePath)
        }
        return file
    }

    fun getLogFile(path: String, context: Context): File? {
        val file = getFile(path, context)
        return if (!file.exists()) {
            null
        } else {
            file
        }
    }


}

sealed class LogEvents(val code: Int) {
    object Connect : LogEvents(0x01)
    object Binding : LogEvents(0x02)
    object SyncData : LogEvents(0x03)
    object Ota : LogEvents(0x04)
    object WatchFace : LogEvents(0x05)
    object Agps : LogEvents(0x06)
}

sealed class SubEvent(val code: Int, var comment: String? = "")

object ConnectEvents {
    object TimeOut : SubEvent(0x01)
    object Failed : SubEvent(0x02)
    object DiscoveryFailed : SubEvent(0x03) //Not using
    object BluetoothEnableFailed : SubEvent(0x04)
    object DeniedByDevice : SubEvent(0xAA)
    object Other : SubEvent(0xFF)
}

object BindingEvents {
    object BindingInfoMatchIssue : SubEvent(0x01)
    object NetworkIssue : SubEvent(0x02)
    object BLEDisconnect : SubEvent(0x03)
    object Other : SubEvent(0xFF)
}

object SyncDataEvents {
    object SyncTimeout : SubEvent(0x01)
    object BLEDisconnect : SubEvent(0x02)
    object Other : SubEvent(0xFF)
}

object OtaEvents {
    object DownloadFileFailed : SubEvent(0x01)
    object BLEDisconnect : SubEvent(0x02)
    object TransferTimeout : SubEvent(0x03)
    object TransferFailed : SubEvent(0x03)
    object Other : SubEvent(0xFF)
}

object WatchFaceEvents {
    object DownloadFileFailed : SubEvent(0x01)
    object BLEDisconnect : SubEvent(0x02)
    object TransferTimeout : SubEvent(0x03)
    object TransferFailed : SubEvent(0x03)
    object Other : SubEvent(0xFF)
}

object AgpsEvents {
    object DownloadFileFailed : SubEvent(0x01)
    object BLEDisconnect : SubEvent(0x02)
    object TransferTimeout : SubEvent(0x03)
    object TransferFailed : SubEvent(0x03)
    object Other : SubEvent(0xFF)
}