package com.noisefit_commans.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.ClassicFlattener
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.Printer
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy
import com.elvishew.xlog.printer.file.naming.FileNameGenerator
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private const val LogsTxtFile = "logs.txt"
private const val ZipFile = "logsZip.zip"
private const val LogsFolder = "logs"

private enum class ErrorType {
    Debug,
    Info,
    Error,
    Verbose,
    Warning
}

const val SUPPORT_EMAIL_ID = "help@nexxbase.com"
const val SUPPORT_MESSAGE = "Logs File"

object FileLogsUtils {

    enum class LogType {
        Watch,
        App
    }

    private var filePrinter: Printer? = null

    /**
     * Compress all files under the specific folder to a single zip file.
     *
     *
     * Should be call in background thread.
     *
     * @param folderPath  the specific folder path
     * @param zipFilePath the zip file path
     * @throws IOException if any error occurs
     * @since 1.4.0
     */

    fun compress(
        folderPath: String,
        zipFilePath: String,
        success: () -> Unit,
        failed: () -> Unit
    ) {
        val folder = File(folderPath)
        if (!folder.exists() || !folder.isDirectory) {
            failed.invoke()
            throw IOException("Folder $folderPath does't exist or isn't a directory")
        }
        val zipFile = File(zipFilePath)
        if (!zipFile.exists()) {
            val zipFolder = zipFile.parentFile
            if (!zipFolder.exists()) {
                if (!zipFolder.createNewFile()) {
                    failed.invoke()
                    throw IOException("Zip folder " + zipFolder.absolutePath + " not created")
                }
            }
            if (!zipFile.createNewFile()) {
                failed.invoke()
                throw IOException("Zip file $zipFilePath not created")
            }
        }
        var bis: BufferedInputStream
        val zos = ZipOutputStream(
            BufferedOutputStream(FileOutputStream(zipFile))
        )
        try {
            val BUFFER_SIZE = 8 * 1024 // 8K
            val buffer = ByteArray(BUFFER_SIZE)
            for (fileName in folder.list()) {
                if (fileName == "." || fileName == "..") {
                    continue
                }
                val file = File(folder, fileName)
                if (!file.isFile) {
                    continue
                }
                val fis = FileInputStream(file)
                bis = BufferedInputStream(fis, BUFFER_SIZE)
                try {
                    val entry = ZipEntry(fileName)
                    zos.putNextEntry(entry)
                    var count: Int
                    while (bis.read(buffer, 0, BUFFER_SIZE).also { count = it } != -1) {
                        zos.write(buffer, 0, count)
                    }
                    success.invoke()
                } finally {
                    try {
                        bis.close()
                    } catch (e: IOException) {
                        // Ignore
                        failed.invoke()
                    }
                }
            }
        } finally {
            try {
                zos.close()
            } catch (e: IOException) {
                failed.invoke()
                // Ignore
            }
        }
    }

    fun composeEmail(
        addresses: String = SUPPORT_EMAIL_ID,
        subject: String = SUPPORT_MESSAGE,
        attachment: Uri,
        context: Context
    ) {

        LOGS.d("File to share $attachment")
        val intent = Intent(Intent.ACTION_SEND).apply {
//            type = "text/*"
            putExtra(Intent.EXTRA_EMAIL, arrayListOf(addresses));
            //data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_STREAM, attachment)
//            clipData = ClipData.newRawUri("", attachment)
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "message/rfc822";
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        context.let {
            if (intent.resolveActivity(it.packageManager) != null) {
                it.startActivity(intent)
            }
        }

    }

    fun checkLogFileExist(
        context: Context,
        colorFitDevice: ColorFitDevice?,
        logFileName: String?
    ): Uri? {
        var path = ""
        colorFitDevice?.deviceType?.let {
            path = if (it == DeviceType.COLORFIT_2.deviceType ||
                it == DeviceType.COLORFIT_PRO_2.deviceType ||
                it == DeviceType.COLORFIT_PRO_3.deviceType ||
                it == DeviceType.COLORFIT_PRO_2_OXY.deviceType ||
                it == DeviceType.NOISEFIT_ACTIVE.deviceType ||
                it == DeviceType.COLORFIT_MIGHTY.deviceType ||
                it == DeviceType.NOISEFIT_NOVA.deviceType ||
                it == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                "$LogsFolder/$logFileName"
            } else {
                "$LogsFolder/$LogsTxtFile"
            }
        }

        return getUri(path, context)
    }

    fun getZipFileUri(context: Context,path: String): Uri? {
        return getUri(path, context)
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
                        AppLogs.FILE_PROVIDER,
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

    fun getZipFilePath(context: Context): File {
        val logFilePath = LogsFolder
        val file = getFile(logFilePath, context)
        val logFile = File(file, ZipFile)
        if (logFile.exists()) {
            logFile.delete()
        }
        logFile.createNewFile()
        return logFile
    }

    private fun getFile(logFilePath: String, context: Context): File {
        val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            File(context.externalCacheDir?.absolutePath, logFilePath)
        } else {
            File(Environment.getExternalStorageDirectory().toString(), logFilePath)

        }
//        if (!file.exists()) {
//            file.mkdirs()
//
//        }

        return file
    }

    fun getFile(
        context: Context, colorFitDevice: ColorFitDevice?,
        logFileName: String?
    ): File? {
        var path = ""
        colorFitDevice?.deviceType?.let {
            path = if (it == DeviceType.COLORFIT_2.deviceType ||
                it == DeviceType.COLORFIT_PRO_2.deviceType ||
                it == DeviceType.COLORFIT_PRO_3.deviceType ||
                it == DeviceType.COLORFIT_PRO_2_OXY.deviceType ||
                it == DeviceType.NOISEFIT_ACTIVE.deviceType ||
                it == DeviceType.COLORFIT_MIGHTY.deviceType ||
                it == DeviceType.NOISEFIT_NOVA.deviceType ||
                it == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                "$LogsFolder/$logFileName"
            }else{
                "$LogsFolder/$LogsTxtFile"
            }
        }

        return getFile(path, context)
    }


    fun getFolderPath(context: Context): File {
        val logFilePath = LogsFolder
        return getFile(logFilePath, context)
    }

    fun initLogs(appContext: Context) {


        val config = LogConfiguration.Builder()
            .logLevel(LogLevel.ALL)          // Specify log level, logs below this level won't be printed, default: LogLevel.AL
            .tag("noise-fit") // Specify TAG, default: "X-LOG"
//            .enableThreadInfo()                                 // Enable thread info, disabled by default
//            .enableStackTrace(4)                                // Enable stack trace info with depth 2, disabled by default
//            .enableBorder()                                     // Enable border, disabled by default
//             .jsonFormatter( MyJsonFormatter())               // Default: DefaultJsonFormatter
//             .xmlFormatter( MyXmlFormatter())                 // Default: DefaultXmlFormatter
//             .throwableFormatter( MyThrowableFormatter())     // Default: DefaultThrowableFormatter
//             .threadFormatter( MyThreadFormatter())           // Default: DefaultThreadFormatter
//             .stackTraceFormatter( MyStackTraceFormatter())   // Default: DefaultStackTraceFormatter
//             .borderFormatter( MyBoardFormatter())            // Default: DefaultBorderFormatter
//             .addObjectFormatter(AnyClass.class,                 // Add formatter for specific class of object
//                  AnyClassObjectFormatter())                  // Use Object.toString() by default
//            .addInterceptor(
//                BlacklistTagsFilterInterceptor(    // Add blacklist tags filter
//                    "blacklist1", "blacklist2", "blacklist3"
//                )
//            )
//            .addInterceptor(
//                WhitelistTagsFilterInterceptor( // Add whitelist tags filter
//                    "whitelist1", "whitelist2", "whitelist3"
//                )
//            )
            // .addInterceptor( MyInterceptor())                // Add a log interceptor
            .build()

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
//                .backupStrategy(FileSizeBackupStrategy2(10000, 20000))
//                .cleanStrategy(FileLastModifiedCleanStrategy())
                .flattener(ClassicFlattener())
                .backupStrategy(NeverBackupStrategy())
                .build()

        this.filePrinter = filePrinter

        XLog.init(config, androidPrinter, filePrinter)

    }

    fun saveDLogs(colorFitDevice: ColorFitDevice?, tag: String, msg: String, logType: LogType) {
        writeLogs(colorFitDevice, tag, msg, logType, ErrorType.Debug)
    }

    fun saveILogs(colorFitDevice: ColorFitDevice?, tag: String, msg: String, logType: LogType) {
        writeLogs(colorFitDevice, tag, msg, logType, ErrorType.Info)
    }

    fun saveWLogs(colorFitDevice: ColorFitDevice?, tag: String, msg: String, logType: LogType) {
        writeLogs(colorFitDevice, tag, msg, logType, ErrorType.Warning)
    }

    fun saveELogs(colorFitDevice: ColorFitDevice?, tag: String, msg: String, logType: LogType) {
        writeLogs(colorFitDevice, tag, msg, logType, ErrorType.Error)
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val backgroundDispatcher = newFixedThreadPoolContext(1, "File Write")
    //TODO convert its operation to background thread
    private fun writeLogs(
        colorFitDevice: ColorFitDevice?,
        tag: String,
        msg: String,
        logType: LogType,
        errorType: ErrorType
    ) {
        scope.launch(backgroundDispatcher) {
                cleanLogFilesIfNecessary()
                val logs =
                    "${logType.name} ${colorFitDevice?.deviceType} $tag ---> $msg"

                when (errorType) {
                    ErrorType.Debug -> {
                        XLog.printers(filePrinter).d(logs)
                    }
                    ErrorType.Error -> {
                        XLog.printers(filePrinter).e(logs)
                    }
                    ErrorType.Info -> {
                        XLog.printers(filePrinter).i(logs)
                    }
                    ErrorType.Verbose -> {
                        XLog.printers(filePrinter).v(logs)
                    }
                    else -> {
                        XLog.printers(filePrinter).d(logs)
                    }
                }
        }
    }

    private fun cleanLogFilesIfNecessary() {
        NoisefitApplication.context?.applicationContext?.let {
            val logDir: File = getFolderPath(it)
            val files = logDir.listFiles() ?: return
            for (file in files) {
                if (file.sizeInMb > 10) {
                    file.delete()
                }
            }
        }

    }

}

val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024
val File.sizeInGb get() = sizeInMb / 1024
val File.sizeInTb get() = sizeInGb / 1024

