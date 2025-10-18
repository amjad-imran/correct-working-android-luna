package com.oreo.ui.device

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit.watch.ConnectionHandler
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.FileLogsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class OMyDeviceViewModel @Inject constructor(
    var connectionHandler: ConnectionHandler,
    val userRepository: UserRepository,
    val sessionManager: SessionManager,
    val ringDataStore: RingDataStore,
    val localDataStore: DataStoredInterface,
    val watchDataStore: WatchDataStore,
    val resProvider: ResourcesProvider,
    val deviceRepository: DeviceRepository,
    val watchesSDK: WatchesSDK,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val appCtx: Context
) : BaseViewModel() {
    private var _deviceConnected: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var deviceConnected = _deviceConnected

    var startWatchFlow: MutableLiveData<Event<Boolean>> = MutableLiveData<Event<Boolean>>()

    var watchLogFile: File? = null
    var appLogFile: File? = null
    var firmwareLogFile: File? = null

    var downloadMyDataSelectedItem: String ?= null
    var downloadMyDataList: ArrayList<String> ?= null

    var downloadMyDataFileUri: Uri ?= null

    val lunarBlackImagesUrl = Pair(
        "https://luna-cdn.gonoise.com/production/ring/set_2/Luna+Gen+2.538+(1)+1.png",
        "https://luna-cdn.gonoise.com/production/ring/set_1/Luna+Gen+2.565+1.png"
    )

    init {
        _deviceConnected.value = (ringDataStore.getRingDevice() != null)
        getLogsPath()
    }

    fun updateDeviceConnectedStatus() {
        _deviceConnected.value = (ringDataStore.getRingDevice() != null)
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
            }

            try {
                val firmwareFile = watchDataStore.getFirmwareLogPath()
                if (!firmwareFile.isNullOrEmpty()) {
                    firmwareLogFile = FileLogsUtils.getFileDirect(
                        watchDataStore.getFirmwareLogPath()!!
                    )
                }
            } catch (ignored: Exception) {
            }

        }
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

                val fileName = watchDataStore.getLogPathName()


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

    fun setDownloadMyDataList(){
        downloadMyDataList = ArrayList<String>().apply {
            this.add(resProvider.getString(R.string.text_today))

            this.add(resProvider.getString(R.string.text_last_val_days, 3))

            this.add(resProvider.getString(R.string.text_last_val_days, 7))
        }

        downloadMyDataSelectedItem = downloadMyDataList?.get(1)
    }

    private val _bsState = MutableStateFlow(DownloadMyDataBS.IDLE)
    val bsState: StateFlow<DownloadMyDataBS> = _bsState

    private val _fileUri = MutableStateFlow<Uri?>(null)
    val fileUri: StateFlow<Uri?> = _fileUri

    fun getDownloadMyDataPDF(days: Int) {
        viewModelScope.launch {
            userRepository.getDownloadMyDataPDF(days).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _bsState.value = DownloadMyDataBS.PROCESSING
                    }
                    is Resource.Success -> {
                        val url = resource.data?.data?.url
                        if (url.isNullOrBlank()) {
                            _bsState.value = DownloadMyDataBS.ERROR
                        } else {
                            downloadIntoCacheAndGetUri(url)
                        }
                    }
                    is Resource.GenericError,
                    is Resource.NetworkError -> {
                        _bsState.value = DownloadMyDataBS.ERROR
                    }
                }
            }
        }
    }

    private fun downloadIntoCacheAndGetUri(pdfUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url(pdfUrl)
                    .header("User-Agent", "okhttp/4 Android")
                    .build()

                okHttpClient.newCall(req).execute().use { res ->
                    if (!res.isSuccessful) error("Download failed: ${res.code}")

                    val filename = "generated_${System.currentTimeMillis()}.pdf"
                    val outFile = File(appCtx.cacheDir, filename)

                    res.body?.byteStream()?.use { input ->
                        FileOutputStream(outFile).use { output ->
                            input.copyTo(output)
                        }
                    } ?: error("Empty body")

                    val uri = FileProvider.getUriForFile(
                        appCtx,
                        "com.noisefit.luna.fileprovider",
                        outFile
                    )

                    withContext(Dispatchers.Main) {
                        _fileUri.value = uri
                        delay(3000L)
                        _bsState.value = DownloadMyDataBS.SUCCESS
                    }
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    _bsState.value = DownloadMyDataBS.ERROR
                }
            }
        }
    }

    fun resetDownloadState() {
        _bsState.value = DownloadMyDataBS.IDLE
        _fileUri.value = null
    }

    fun handleOkayBtnBsClicked(){
        _bsState.value = DownloadMyDataBS.OPEN_PDF
        resetDownloadState()
    }

    enum class DownloadMyDataBS { IDLE, PROCESSING, SUCCESS, OPEN_PDF, ERROR }

}