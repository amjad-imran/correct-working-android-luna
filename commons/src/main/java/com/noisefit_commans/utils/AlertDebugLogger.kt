package com.noisefit_commans.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.models.DeviceAlertFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ALERT_LOGS_FOLDER = "alertLogs"
private const val ALERT_LOG_FILE = "alert_debug.log"

object AlertDebugLogger {

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val backgroundDispatcher = newFixedThreadPoolContext(1, "AlertDebugLogs")
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun log(source: String, message: String) {
        val context = NoisefitApplication.context?.applicationContext ?: return
        scope.launch(backgroundDispatcher) {
            val file = getFile(context)
            file.parentFile?.mkdirs()
            if (!file.exists()) {
                file.createNewFile()
            }
            file.appendText("${timeFormat.format(Date())} | $source | $message\n")
        }
    }

    fun logValue(source: String, label: String, value: Any?) {
        log(source, "$label=${toJson(value)}")
    }

    fun logAlertFlow(
        source: String,
        feature: DeviceAlertFeature,
        operationId: Long?,
        stage: String,
        message: String
    ) {
        log(
            source,
            "feature=$feature op_id=${operationId ?: 0L} stage=$stage $message"
        )
    }

    fun logAlertFlowValue(
        source: String,
        feature: DeviceAlertFeature,
        operationId: Long?,
        stage: String,
        label: String,
        value: Any?
    ) {
        logAlertFlow(
            source = source,
            feature = feature,
            operationId = operationId,
            stage = stage,
            message = "$label=${toJson(value)}"
        )
    }

    fun clear() {
        val context = NoisefitApplication.context?.applicationContext ?: return
        scope.launch(backgroundDispatcher) {
            val file = getFile(context)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    fun getFileUri(context: Context): Uri? {
        val file = getFile(context)
        if (!file.exists()) {
            return null
        }
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, AppLogs.FILE_PROVIDER, file)
            } else {
                Uri.fromFile(file)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getFile(context: Context): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            File(context.externalCacheDir?.absolutePath, "$ALERT_LOGS_FOLDER/$ALERT_LOG_FILE")
        } else {
            File(Environment.getExternalStorageDirectory().toString(), "$ALERT_LOGS_FOLDER/$ALERT_LOG_FILE")
        }
    }

    private fun toJson(value: Any?): String {
        return try {
            gson.toJson(value)
        } catch (_: Exception) {
            value?.toString() ?: "null"
        }
    }
}
