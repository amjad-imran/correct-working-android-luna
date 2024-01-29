package com.noisefit.receiver.workManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.FileLogsUtils
import com.noisefit_commans.utils.LOGS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

@HiltWorker
class FeedbackSubmitWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    val ringDataStore: RingDataStore,
    val watchDataStore: WatchDataStore,
    val deviceRepository: DeviceRepository
) : CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        LOGS.w("FeedbackSubmitWorker", "Worker running")
        try {
            getFileLogs().collect { files ->
                sendFeedback(files.first, files.second, files.third, this)
            }

            Result.success()
        } catch (e: Exception) {
            // Handle errors and return Result.FAILURE
            Result.failure()
        }
    }

    private suspend fun getFileLogs(): Flow<Triple<File?, File?, File?>> {
        return flow {

            val context = NoisefitApplication.context!!
            var appLogs: File? = null
            try {
                appLogs = AppLogs.getFile(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            var firmwareLogs: File? = null

            try {
                val firmwareFile = watchDataStore.getFirmwareLogPath()
                if (!firmwareFile.isNullOrEmpty()) {
                    firmwareLogs = FileLogsUtils.getFileDirect(
                        watchDataStore.getFirmwareLogPath()!!
                    )
                }
            } catch (ignored: Exception) {
            }


            var watchLogs: File? = null
            try {

                if (FileLogsUtils.checkLogFileExist(
                        context,
                        ringDataStore.getRingDevice(),
                        watchDataStore.getLogPathName()
                    ) != null
                ) {
                    watchLogs = FileLogsUtils.getFile(
                        context,
                        ringDataStore.getRingDevice(),
                        watchDataStore.getLogPathName()
                    )
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }

            emit(Triple(appLogs, watchLogs, firmwareLogs))
        }
    }

    private suspend fun sendFeedback(
        appLogFile: File?,
        watchLogFile: File?,
        firmwareLogs: File?,
        scope: CoroutineScope
    ) {

        scope.launch {

            val tempAppLogFile =
                async(Dispatchers.IO) { createTempAppLogFile("tempAppLogs", appLogFile) }
            val tempRingLogFile = async { createTempAppLogFile("tempRingLogs", watchLogFile) }
            val tempFirmwareLogFile =
                async { createTempAppLogFile("tempFirmwareLogs", firmwareLogs) }


            deviceRepository.periodicFeedbackFile(
                tempAppLogFile.await(), tempRingLogFile.await(), tempFirmwareLogFile.await()
            ).collect { resource ->


                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let {

                            val appFile = tempAppLogFile.await()
                            val ringFile = tempRingLogFile.await()
                            val firmwareFile = tempFirmwareLogFile.await()
                            if (appFile?.exists() == true) {
                                appFile.delete()
                            }

                            if (ringFile?.exists() == true) {
                                ringFile.delete()
                            }
                            if (firmwareFile?.exists() == true) {
                                firmwareFile.delete()
                            }


                            ringDataStore.saveAutoLogsTimeStamp()

                            /*if (problemType.equals(ProblemType.WATCHFACE_TRANSFER.name, true)) {
                                lastSyncProvider.setSyncTimeStamp(LastSyncItems.WATCHFACE_FEEDBACK)
                            } else if (problemType.equals(ProblemType.PAIRING.name, true)) {
                                lastSyncProvider.setSyncTimeStamp(LastSyncItems.PAIRING_FEEDBACK)
                            }*/
                        }
                    }

                    else -> {
                        LOGS.e("Failed")
                    }
                }

            }
        }
    }

    private suspend fun createTempAppLogFile(fileName: String, originalFile: File?): File? {
        if (originalFile == null) {
            return null
        }
        if (!originalFile.exists()) {
            return null
        }

        val file = File.createTempFile(fileName, ".txt", context.cacheDir);
        try {
            var sourceChannel: FileChannel? = null
            var destChannel: FileChannel? = null

            try {
                sourceChannel = withContext(Dispatchers.IO) {
                    FileInputStream(originalFile).channel
                }
                destChannel = withContext(Dispatchers.IO) {
                    FileOutputStream(file).channel
                }
                withContext(Dispatchers.IO) {
                    sourceChannel.transferTo(0, sourceChannel.size(), destChannel)
                }
            } finally {
                sourceChannel?.close()
                destChannel?.close()
            }
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }


}