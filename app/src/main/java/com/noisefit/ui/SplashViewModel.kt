package com.noisefit.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.JsonObject
import com.noisefit.luna.BuildConfig
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.AppRepository
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import com.noisefit.util.FirebaseCrashlyticsUtils
import com.noisefit.watch.ApplicationHandler
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.base.BaseInitializeCallbacks
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanN
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
@Inject constructor(
    private val lastSyncProvider: LastSyncProvider,
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    private val watchDataStore: WatchDataStore,
    private val applicationHandler: ApplicationHandler,
    private val appRepository: AppRepository,
    private val deviceRepository: DeviceRepository,
    private val userHealthDataDataSource: OreoUserHealthDataDataSource,
    val sessionManager: SessionManager,
    private val firebaseCrashlyticsUtils: FirebaseCrashlyticsUtils
) : BaseViewModel() {
    var connectedDevice: ColorFitDevice? = null
    var notificationType: String? = null
    var notificationIndex: String? = null
    var appLink: AppLinks? = null
    private var ignoredVersion = 0
    private val _userOnBoardingFlow = MutableLiveData<UserOnBoardingFlow>()
    var userOnBoardingFlow = _userOnBoardingFlow

    init {
        //Don't remove below line
        if (localDataStore.getLastStepsSyncWithServer() == 0L) {
            localDataStore.setLastStepsSyncWithServer(DateFormats.getTimeStamp())
        }
        connectedDevice = ringDataStore.getRingDevice()
        clearOldTableData()
        enableOldNotifications()
        WatchInfoGlobals.hideBleCallingDialogForThisSession = false
        firebaseCrashlyticsUtils.setCrashlyticsUserProperty()
    }

    fun initFirstOpen() {
        if (localDataStore.getFirstOpenTimeStamp() == 0L) {
            localDataStore.setFirstOpenTimeStamp(DateFormats.getTimeStamp())
        }
    }

    private fun enableOldNotifications() {
        val isNotificationAlertEnable = localDataStore.isNotificationAlertEnabled()
        val enabledList = localDataStore.getOldNotificationEnabledAppList()

        if (enabledList.isNullOrEmpty() || !isNotificationAlertEnable) {
            LOGS.d("no notification list")
            localDataStore.clearOldNotificationAppList()
            return
        }
        GlobalScope.launch {
            //isCallEnabled, isSmsEnabled, willEnableNotificationsList
            deviceRepository.enableInstalledAppsNotification(enabledList).collect { data ->
                if (data.first) {
                    localDataStore.setCallAlertStatus(true)
                }
                if (data.second) {
                    localDataStore.setSMSAlertStatus(true)
                }
                if (data.third.isNotEmpty()) {
                    localDataStore.setNotificationAlertStatus(true)
                    localDataStore.saveNotificationAppList(data.third)
                }
                localDataStore.clearOldNotificationAppList()
            }
        }
    }


    fun updatePrivacyPolicyStatus(status: Boolean) {
        localDataStore.setPrivacyPolicyStatus(status)
    }


    private fun subscribeToTopic() {
        Firebase.messaging.subscribeToTopic("weather").addOnCompleteListener { task ->
            var msg = "Subscribed"
            if (!task.isSuccessful) {
                msg = "Subscribe failed"
            }
            LOGS.d("subscribeToTopic:: ${msg}")

        }
    }

    fun getDeviceFeatures(colorFitDevice: ColorFitDevice) {
        viewModelScope.launch {
            deviceRepository.getDeviceFeature(colorFitDevice.deviceId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.deviceFeatures?.let {
                            ringDataStore.saveDeviceFeatures(it)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun checkAppVersion() {

        connectedDevice?.let {
            val lastFetchTimeStamp = localDataStore.getDeviceFeaturesLastSyncTime()
            val fetchPeriod = localDataStore.getFeatureIntervalFetchPeriod()
            if (fetchPeriod != 0) {
                if (lastFetchTimeStamp.checkTimeDifferenceMoreThanN(fetchPeriod)) {
                    getDeviceFeatures(it)
                }
            }

        }

        val requestObject = JsonObject().apply {
            addProperty("platform", "android")
            addProperty("version", BuildConfig.VERSION_CODE)
            addProperty("version_name", BuildConfig.VERSION_NAME)
            connectedDevice?.let {
                addProperty("device_type", it.deviceType)
                watchDataStore.getDeviceFirmwareDetails()?.let { firmware ->
                    val deviceDetailsObj = JsonObject().apply {
                        //addProperty("version", 111)
                        this.addProperty("version", firmware.version)
                        this.addProperty("firmware_id", firmware.firmwareId)
                        this.addProperty("mac", it.address)
                    }
                    add("deviceDetails", deviceDetailsObj)
                }
            }
        }
        GlobalScope.launch {
            appRepository.checkAppVersion(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        sendMessage("")
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {


                            if (it.otaResponse != null) {
                                sessionManager.forceOtaFlowRunning = false
                                sessionManager.forceOtaResponseRing = it.otaResponse

                            } else {
                                sessionManager.forceOtaFlowRunning = false
                                sessionManager.forceOtaResponseRing = null
                            }

                            localDataStore.saveFeatureIntervalFetchPeriod(it.resetInterval ?: 24)
                            localDataStore.saveLogSyncInterval(it.logsSyncInterval ?: 2)

                            handleUserHealthCache(it.cacheVersion ?: 1)

                            //localDataStore.saveHistoryYears(it.calendarYears ?: 2)

                            val helpUpdateTimStamp = if (it.helpUpdate.isNullOrEmpty()) {
                                0L
                            } else {
                                try {
                                    it.helpUpdate?.toLongOrNull()
                                } catch (exp: Exception) {
                                    0L
                                }
                            }

                            lastSyncProvider.setSyncTimeStamp(
                                LastSyncItems.H_AND_SUPPORT_SERVER_UPDATE,
                                helpUpdateTimStamp ?: 0L
                            )



                            handleAppVersion(versionCheckResponse = it)
                        } ?: checkAppVersion()
                    }
                }
            }
        }

    }

    private fun handleUserHealthCache(cacheVersion: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val storedCacheVersion = localDataStore.getUserHealthCacheVersion()
            if (storedCacheVersion != cacheVersion) {
                AppLogs.sendAppLogs("App User health cache cleared User:$cacheVersion Server:$cacheVersion")
                userHealthDataDataSource.clearAllData()
                localDataStore.setUserHealthCacheVersion(cacheVersion)
            }
        }
    }

    private fun handleAppVersion(versionCheckResponse: VersionCheckResponse) {

        if (versionCheckResponse.maintenanceMode == true) {
            sessionManager.versionCheckData.postValue(versionCheckResponse)
            return
        }

        if (versionCheckResponse.upgradeType?.lowercase() == "force_upgrade") {
            sessionManager.versionCheckData.postValue(versionCheckResponse)
            return
        } else if (versionCheckResponse.upgradeType?.lowercase() == "soft_upgrade") {

            if (ignoredVersion != versionCheckResponse.currentVersion) {
                sessionManager.versionCheckData.postValue(versionCheckResponse)
                return
            }

        }

        sessionManager.versionCheckData.postValue(
            VersionCheckResponse(
                null, null, null, null, null, null
            )
        )

    }

    private fun clearOldTableData() {

        if (!localDataStore.getLastClearTables().checkDayDifferenceMoreOne()) {
            return
        }
        localDataStore.setLastClearTables(DateFormats.getTimeStamp())
        viewModelScope.launch {
            appRepository.deleteOldTableData().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        LOGS.d("Deleted old tables")
                    }

                    is CacheResult.GenericError -> {
                        LOGS.d("Error in Deleting old tables")
                    }
                }

            }
        }
    }

    fun setIgnoreVersion(ignoreVersion: Int) {
        this.ignoredVersion = ignoreVersion
    }

    fun checkOnBoardingFlow() {

        val privacyPolicyAccepted = localDataStore.getPrivacyPolicyStatus()
        connectedDevice?.let {
            applicationHandler.initSdks(connectedDevice).apply {
                this?.callbackListener(object : BaseInitializeCallbacks {
                    override fun serviceConnected() {
                        AppLogs.sendAppLogs("serviceConnected")
                    }

                    override fun serviceDisconnected() {
                        AppLogs.sendAppLogs("serviceDisconnected")
                    }

                })
            }
        }
        if (!privacyPolicyAccepted) {
            _userOnBoardingFlow.value = (UserOnBoardingFlow.ACCEPT_PRIVACY_POLICY)
            return
        }

        val userLogin = localDataStore.getUser()
        if (userLogin == null) {
            _userOnBoardingFlow.value = (UserOnBoardingFlow.ASK_FOR_LOGIN)
            return
        }

        val isPreviouslyPaired = localDataStore.isPreviouslyPaired()

        if (ringDataStore.getRingDevice() == null && !isPreviouslyPaired) {
            _userOnBoardingFlow.value = (UserOnBoardingFlow.PAIR_DEVICE)
            return
        }

        if (!isProfileSetupComplete()) {
            _userOnBoardingFlow.value = (UserOnBoardingFlow.SETUP_PROFILE)
            return
        }

        val localUserData = localDataStore.getLocalUserData()
        if (localUserData != null) {
            _userOnBoardingFlow.value = (UserOnBoardingFlow.SETUP_PROFILE)
            return
        }

        val deviceSetupStatus = localDataStore.getDeviceSetupStatus()
        if (ringDataStore.getRingDevice() != null && deviceSetupStatus == 1) {
            _userOnBoardingFlow.value = (UserOnBoardingFlow.DEVICE_SETUP)
            return
        }

        _userOnBoardingFlow.value = (UserOnBoardingFlow.SHOW_OREO_DASHBOARD)

    }

    private fun isProfileSetupComplete(): Boolean {
        val user = localDataStore.getUser() ?: return false
        if (user.userInfo?.dob.isNullOrEmpty() || (user.userInfo?.height
                ?: 0) == 0 || (user.userInfo?.weight ?: 0) == 0 || (user.userGoals?.caloriesGoal
                ?: 0) == 0
        ) {
            return false
        }
        return true
    }

}

enum class UserOnBoardingFlow {
    SHOW_OREO_DASHBOARD, ASK_FOR_LOGIN, SETUP_PROFILE, ACCEPT_PRIVACY_POLICY, PAIR_DEVICE, DEVICE_SETUP
}

enum class AppLinks {
    REFERRAL, NOTIFICATION_CONTROL, PROFILE, SLEEP_PLANNER, DASHBOARD, LUNA_AI, FEATURE_REQUEST, FEMALE_HEALTH, CAFFEINE_WINDOW,
    ADD_CAFFEINE_INTAKE, ADD_LIGHT_EXPOSURE, ADD_WORKOUT,
    ADD_MEAL, ADD_RECOVERY, ADD_SUPPLEMENTS, ADD_ALCOHOL,
    LOG_PERIOD_SYMPTOMS, ADD_SLEEP,
    SLEEP_DASH, READINESS_DASH, ACTIVITY_DASH,
    CIRCADIAN_PAGE, STRESS_DETAIL, HR_DETAIL, TIMELINE_LIST
}