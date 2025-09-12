package com.oreo.ui.device

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit.watch.ConnectionHandler
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.models.CaseInfoData
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.FileLogsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
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
    val watchesSDK: WatchesSDK
) : BaseViewModel() {
    private var _deviceConnected: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var deviceConnected = _deviceConnected

    var startWatchFlow: MutableLiveData<Event<Boolean>> = MutableLiveData<Event<Boolean>>()

    var watchLogFile: File? = null
    var appLogFile: File? = null
    var firmwareLogFile: File? = null

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


}