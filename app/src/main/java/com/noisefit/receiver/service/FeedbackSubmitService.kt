package com.noisefit.receiver.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.LifecycleService
import com.noisefit_commans.data.model.Feedback
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.FileLogsUtils
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
import java.util.ArrayList
import javax.inject.Inject

@AndroidEntryPoint
class FeedbackSubmitService @Inject
constructor() : LifecycleService() {

    @Inject
    lateinit var deviceRepository: DeviceRepository

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var ringDataStore: RingDataStore

    @Inject
    lateinit var watchDataStore: WatchDataStore

    @Inject
    lateinit var lastSyncProvider: LastSyncProvider

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)


    companion object {
        fun startService(
            context: Context,
            problemType: String,
            comment: String,
            deviceId: Int? = null
        ) {
            context.startService(Intent(context, FeedbackSubmitService::class.java).apply {
                this.putExtra("problemType", problemType)
                this.putExtra("comment", comment)
                this.putExtra("deviceId", deviceId)
            })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val problemType = intent?.getStringExtra("problemType")
        val comment = intent?.getStringExtra("comment")
        val deviceId = intent?.getIntExtra("deviceId", -1)


        scope.launch {
            getFileLogs().collect { files ->

                sendFeedback(files.first, files.second, problemType ?: "", comment ?: "", deviceId)

            }
        }


        return super.onStartCommand(intent, flags, startId)
    }

    suspend fun getFileLogs(): Flow<Pair<File?, File?>> {
        return flow {

            val context = NoisefitApplication.context!!
            var appLogs: File? = null
            try {
                appLogs = AppLogs.getFile(context)
            } catch (e: Exception) {
                e.printStackTrace()
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

            emit(Pair(appLogs, watchLogs))
        }
    }


    private fun sendFeedback(
        appLogFile: File?,
        watchLogFile: File?,
        problemType: String,
        comment: String,
        deviceId: Int? = null
    ) {

        val feedback =
            provideFeedbackData(
                problemType,
                comment,
                ArrayList<Uri>(),
                appLogFile,
                watchLogFile
            )

        feedback.user_id = localDataStore.getUser()?.id

        scope.launch {
            deviceRepository.submitFeedback(
                feedback,
                if (deviceId == -1) null else deviceId
            ).collect { resource ->


                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let {
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun provideFeedbackData(
        problemType: String,
        comment: String,
        screenShortList: List<Uri>,
        file: File?,
        watchLogs: File?
    ): Feedback {
        val packageInfo = NoisefitApplication.context!!.packageManager.getPackageInfo(
            NoisefitApplication.context!!.packageName,
            0
        )
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.versionCode


        val connectedDevice = ringDataStore.getRingDevice()

        val platform = "android"
        val mobileDevice = "${Build.BRAND} ${Build.MODEL}"
        val osVersion = Build.VERSION.RELEASE
        val appVersion = "$versionName($versionCode)"
        val watchName = connectedDevice?.bluetoothName.toString()
        val watchFirmwareVersion =
            WatchInfoGlobals.firmwareVersionRing
                ?: WatchInfoGlobals.firmwareVersionNumberRing.toString()



        LOGS.d("connectedDevice $watchName")
        return Feedback(
            platform,
            mobileDevice,
            osVersion,
            appVersion,
            watchName,
            watchFirmwareVersion,
            problemType,
            comment,
            DateFormats.getTodaysDateString(9),
            screenShortList,
            file,
            watchLogs
        )
    }

}

enum class ProblemType {
    WATCHFACE_TRANSFER, PAIRING
}