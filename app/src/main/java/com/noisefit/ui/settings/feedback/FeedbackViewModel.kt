package com.noisefit.ui.settings.feedback

import android.net.Uri
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.Feedback
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.session.SessionManager
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.FileLogsUtils
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel
@Inject
constructor(
    private val localDataStore: DataStoredInterface,
    private val watchDataStore: WatchDataStore,
    private val ringDataStore: RingDataStore,
    private val deviceRepository: DeviceRepository,
    val sessionManager: SessionManager,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {


    var watchLogFile: File? = null
    var appLogFile: File? = null


    var mightyLogPath: String? = null

    var uploadingFile = false

    var photoFile: File? = null
    var photoURI: Uri? = null
    var outputUri: Uri? = null
    var submittedSuccessfully = MutableLiveData<Boolean>()


    var problemTypeList = HashSet<String>()
    val feedbackUriList = MutableLiveData<ArrayList<Uri>>()

    var showAttachLogButton = MutableLiveData<Boolean>(false)

    init {

        if (watchesSDK.getWatchType() == SDKWatchType.SDK_RYEEX) {
            setLoading(true)
            sessionManager.sendQueryAction(QueryAction.GetFirmwareLogs)
        } else {
            getLogsPath()
        }

    }

    fun getLogsPath() {
        viewModelScope.launch {
            getFileLogs().collect { files ->
                appLogFile = files.first
                watchLogFile = files.second

                var hasLogFiles = false
                if (appLogFile?.exists() == false) {
                    appLogFile = null
                } else {
                    hasLogFiles = true
                }
                if (watchLogFile?.exists() == false) {
                    watchLogFile = null
                } else {
                    hasLogFiles = true
                }

                if (hasLogFiles) {
                    showAttachLogButton.value = true
                }


            }
        }
    }


    fun addToScreenshot(uri: Uri) {
        if (feedbackUriList.value == null) {
            feedbackUriList.postValue(ArrayList<Uri>().apply {
                this.add(uri)
            })
        } else {
            feedbackUriList.postValue(feedbackUriList.value.apply {
                this?.add(uri)
            })
        }
    }


    fun getLocalDataStore(): DataStoredInterface {
        return localDataStore
    }


    fun submitReview(
        feedback: Feedback
    ) {

        feedback.user_id = localDataStore.getUser()?.id

        viewModelScope.launch {
            uploadingFile = true
            deviceRepository.submitFeedback(
                feedback
            ).collect { resource ->

                when (resource) {
                    is Resource.GenericError -> {
                        uploadingFile = false
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        uploadingFile = false
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        submitReview(
                                            feedback
                                        )
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        uploadingFile = false
                        resource.data?.let {
                            LOGS.d("SAVED USER DATA")
                            submittedSuccessfully.postValue(true)
                        } ?: sendMessage("Something went wrong")
                    }
                }

            }
        }

    }

    fun provideFeedbackData(
        problemType: String,
        comment: String,
        screenShortList: List<Uri>,
        file: File?,
        watchLogs: File?
    ): Feedback {
        val packageInfo = NoisefitApplication.context!!.packageManager.getPackageInfo(
            NoisefitApplication.context!!.packageName, 0
        )
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.versionCode


        val connectedDevice =

                ringDataStore.getRingDevice()



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


    private suspend fun getFileLogs(): Flow<Pair<File?, File?>> {
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

                var fileName = watchDataStore.getLogPathName()
                if (!mightyLogPath.isNullOrEmpty()) {
                    fileName = mightyLogPath
                }


                if (FileLogsUtils.checkLogFileExist(
                        context,
                        ringDataStore.getRingDevice(),
                        fileName
                    ) != null
                ) {


                    watchLogs = FileLogsUtils.getFile(
                        context,
                        ringDataStore.getRingDevice(),
                        fileName
                    )
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }

            emit(Pair(appLogs, watchLogs))
        }
    }


}