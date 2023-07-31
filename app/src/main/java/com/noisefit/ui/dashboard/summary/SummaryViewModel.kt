package com.noisefit.ui.dashboard.summary

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit_commans.data.model.*
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.Streaks
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.receiver.workManager.HealthOverviewDataType
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.PromotionalUtil
import com.noisefit.watch.CallingWatchUtils
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.response.LiveMatch
import com.noisefit_commans.data.response.NplLeague
import com.noisefit_commans.data.response.PrizeInfo
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject


@HiltViewModel
class SummaryViewModel
@Inject constructor(
    private val userRepository: UserRepository,
    private val rewardsRepository: RewardsRepository,
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK,
    private val dataUnitConverter: DataUnitConverter,
    private val callingWatchUtils: CallingWatchUtils,
    @ApplicationContext private val appContext: Context
) : BaseViewModel() {

    var isAlertExpanded: Boolean = false
    var summary = Summary()

    var dashboardAlerts = MutableLiveData<List<DashNotification>>()

    var rewardsPoints = MutableLiveData<Long>()
    var rewardAwaited = MutableLiveData<Boolean>(false)
    var streaks = MutableLiveData<Streaks?>()

    val iplData = MutableLiveData<Pair<PrizeInfo?, LiveMatch?>>()
    val _nplLeagueData = MutableLiveData<NplLeague?>()
    val nplLeagueData: LiveData<NplLeague?>
        get() = _nplLeagueData

    fun initData() {
        summary.user = getUser()


//        summary.user?.userGoals?.stepGoal = 15
//        localDataStore.saveUserInfo(summary.user!!)
        summary.connectedDevice = getDeviceConnected()
        summary.deviceFeatures = getDeviceFeatureList()
        //    LOGS.d("userINFO_SUMMARY ${summary.deviceFeatures}")
//        summary.editHealthOverView = localDataStore.getEditHealthOverView()
        summary.bleCallingStatus = callingWatchUtils.getBleCallStatus()
        /*if (summary.recentActivities.value?.activities.isNullOrEmpty()) {
            getRecentWorkouts()
        }*/
        showPromotionalBanner()

        /*if (summary.displayBanners.value.isNullOrEmpty()) {
            val lastFetchTime = localDataStore.getLastBannerApiCallFetchTime()
            val installHours =
                TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastFetchTime)
            if (installHours >= 24) {
                getShopBanners()
                localDataStore.setLastBannerApiCallFetchTime(DateFormats.getTimeStamp())
            } else {
                val temp = localDataStore.getBannerList()
                summary.displayBanners.postValue(temp)
            }
        }*/

    }

    fun isCheckActivityProgressZero(): Boolean {
        val healthData = summary.healthOverviewData.value
        val distanceFloat = try {
            healthData?.distance?.toFloat()
        } catch (exp: Exception) {
            0.0f
        }
        return healthData?.calories == 0 && healthData.steps == 0 && distanceFloat == 0.0f
    }

    private fun getDeviceFeatureList(): DeviceFeatures {
        val deviceFeatures = localDataStore.getDeviceFeatures()
        if (deviceFeatures != null) {
            deviceFeatures.calorieData = 0
            if (watchesSDK.isCaloriesSupported()) {
                deviceFeatures.calorieData = 1
            }
            return deviceFeatures
        }
        //feed dummy data in device features

        return DeviceFeatures(
            stepsData = 1,
            heartRate = 1,
            sleepData = 1,
            bloodOxygen = 1,
            stressCount = 1,
            bodyTemperature = 0,
            calorieData = 0
        )
    }

    fun getInitialOfflineData() {

        if (summary.deviceFeatures == null) {
            summary.deviceFeatures = getDeviceFeatureList()
        }


        viewModelScope.launch(Dispatchers.IO) {
            val userActivities = userRepository.getSummaryHealthOverview(
                summary.deviceFeatures!!
            )
            summary.refreshPosition = null
            summary.healthOverviewData.postValue(userActivities)
        }
    }

    fun getUserActivities(healthOverviewDataType: HealthOverviewDataType) {
        if (summary.healthOverviewData.value?.healthOverviewList.isNullOrEmpty()) {
            getInitialOfflineData()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val userActivities = userRepository.getHealthOverview(
                healthOverviewDataType, summary.healthOverviewData.value
            )
            summary.refreshPosition = userActivities.second
            summary.healthOverviewData.postValue(userActivities.first)
        }
    }


    fun getDeviceConnected(): ColorFitDevice? {
        return localDataStore.getConnectedDevice()
    }


    fun getUserHeaderTitle(context: Context, connected: Boolean): Triple<String, String, String> {
        val greetingMsg = getGreetingMessage()
        val userLogin = summary.user != null
        val imageLink = summary.user?.imageUrl ?: ""
        var title = context.getString(R.string.text_hi_stranger, greetingMsg)
        var subHeading =
            context.getString(R.string.text_set_up_your_profile_for_a_better_experience)
        if (userLogin) {
            title =
                "$greetingMsg, ${summary.user?.getOnlyFirstName()?.trim()?.ifEmpty { "Stranger" }}"
            subHeading = if (!connected) {
                context.getString(R.string.text_pair_your_device_and_crush_your_fitness)
            } else {
                context.getString(R.string.text_crush_your_fitness_goals_and_keep_track_of_your_wins)
            }

        }



        return Triple(title, subHeading, imageLink)
    }


    fun getRecentWorkouts() {

        viewModelScope.launch {
            userRepository.getSummaryRecentActivities(false).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getRecentWorkouts()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            handleRecentActivities(it)

                        }
                    }
                }
            }
        }

    }

    fun getRewardsData() {

        viewModelScope.launch {
            rewardsRepository.getDashboardRewardsData().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getRewardsData()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            iplData.postValue(Pair(it.prizeInfo, it.liveMatch))
                            rewardsPoints.postValue(it.points)
                            rewardAwaited.postValue(it.rewardAwaiting)
                            streaks.postValue(it.streak)

                        }
                    }
                }
            }
        }

    }

    fun getShopBanners() {
        viewModelScope.launch {
            userRepository.getDashboardBanner().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getShopBanners()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            //summary.displayBanners.postValue(it)
                            localDataStore.setBannerList(it as ArrayList<DashboardBanner>)
//                            if (!it.isNullOrEmpty()) {
//                                summary.shopBanners = it
//                                timer.cancel()
//                                loadImage()
//                            }


                        }
                    }
                }
            }
        }

    }

//    fun loadImage() {
//        timer.start()
//    }
//
//    private val timer = object : CountDownTimer(5000, 1000) {
//        override fun onTick(millisUntilFinished: Long) {
//        }
//
//        override fun onFinish() {
//            try {
//                summary.displayBanners.value = (summary.shopBanners[summary.imageCounter])
//                if (summary.imageCounter < (summary.shopBanners.size - 1)) {
//                    summary.imageCounter++
//                } else {
//                    summary.imageCounter = 0
//                }
//
//                loadImage()
//            } catch (exp: Exception) {
//                exp.printStackTrace()
//            }
//        }
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        timer.cancel()
//    }

    private fun handleRecentActivities(recentActivities: RecentActivities) {
        if (recentActivities.activities.isNullOrEmpty()) {
            recentActivities.activities = ArrayList()
            recentActivities.activities!!.add(getRecentListDummyActivity())
        } else {
            recentActivities.activities?.forEach { sportsModeResponse ->
                if (sportsModeResponse.distance != null && sportsModeResponse.distance!! > 0) {
                    val units = summary.user?.userGoals?.getUnit() ?: Units.METRIC
                    sportsModeResponse.formattedData = dataUnitConverter.formatDistance(
                        sportsModeResponse.distance?.toInt() ?: 0, units
                    )
                    sportsModeResponse.formattedDataUnit = dataUnitConverter.distanceUnit(units)
                } else {
                    sportsModeResponse.formattedData = sportsModeResponse.calories.toString()
                    sportsModeResponse.formattedDataUnit = "kcal"
                }
            }
        }
        summary.recentActivities.postValue(recentActivities)
    }

    fun updateBluetoothDialogState(state: Boolean) {
        localDataStore.setBluetoothDialogShown(state)
    }

    private fun getRecentListDummyActivity(): SportsModeResponse {
        return SportsModeResponse(activityType = "No data")
    }

    fun getUser(): User? {
        return localDataStore.getUser()
    }

    var imageTimer: Timer? = null

    var nplTimer: Timer? = null

    fun startBannerTimer() {
        if (imageTimer == null) {
            imageTimer = Timer()
            imageTimer?.scheduleAtFixedRate(RemindTask(), 0, 8000)
        }
    }

    fun startNplTimer() {
        if (nplTimer == null) {
            nplTimer = Timer()
            nplTimer?.scheduleAtFixedRate(NplScrollTask(), 0, 4000)
        }
    }

    fun getAlerts() {

        val notificationList = ArrayList<DashNotification>()

        if (localDataStore.getConnectedDevice() == null) {
            dashboardAlerts.postValue(notificationList)
            return
        }
        val isDeviceSetupPending = localDataStore.getDeviceSetupPendingStatus()
        if (isDeviceSetupPending) {
            notificationList.add(
                DashNotification(
                    0,
                    appContext.getString(R.string.text_notifications_device_setup_pending),
                    DashNotificationType.DEVICE_SETUP,
                    true
                )
            )
        }

        val isBatteryOptimisationStatus = localDataStore.getBatteryOptimisationStatus()
        if (!ApplicationUtils.isIgnoringBatteryOptimizations(NoiseFitApplicationMain.context!!) && !ignoreBatteryOptimizationForSomeManufacturer() && !isBatteryOptimisationStatus) {
            notificationList.add(
                DashNotification(
                    0,
                    appContext.getString(R.string.text_background_optimization),
                    DashNotificationType.BATTERY_OPTIMIZATION
                )
            )
        }

        /*   val backgroundAlertStatus = localDataStore.getEnableBgPermissionDialog()

           if (!backgroundAlertStatus) {
               notificationList.add(
                   DashNotification(
                       0,
                       appContext.getString(R.string.text_background_optimization),
                       DashNotificationType.BACKGROUND_ALERT
                   )
               )
           }*/


        val isNotificationVisible = if (localDataStore.isNotificationAlertEnabled()) {
            false
        } else {
            !localDataStore.getNotificationMessageClearStatus()
        }
        if (isNotificationVisible) {
            notificationList.add(
                DashNotification(
                    0,
                    appContext.getString(R.string.text_notifications_is_disabled_please_click_here_to_enable),
                    DashNotificationType.NOTIFICATION
                )
            )
        }


        if (!checkBackgroundLocationPermission()) {
            notificationList.add(getBackgroundPermissionAlert())
        }
        if (!localDataStore.getAlertSupportStatus()) {
            notificationList.add(
                DashNotification(
                    0,
                    appContext.getString(R.string.text_alert_support),
                    DashNotificationType.SUPPORT_QUERIES
                )
            )
        }
        dashboardAlerts.value = notificationList
        dashboardAlerts.postValue(notificationList)
    }

    private fun getBackgroundPermissionAlert(): DashNotification {
        return DashNotification(
            0,
            appContext.getString(R.string.text_background_location_permission_required),
            DashNotificationType.BACKGROUND_PERMISSION,
            true
        )
    }

    fun handleBackGroundPermissionAlert() {
        var indexToRemove = -1
        dashboardAlerts.value?.forEachIndexed { index, dashNotification ->
            if (dashNotification.type == DashNotificationType.BACKGROUND_PERMISSION) {
                indexToRemove = index
                return@forEachIndexed
            }
        }
        if (checkBackgroundLocationPermission()) {
            if (indexToRemove != -1) {
                removeDashboardAlert(indexToRemove)
            }
        } else {
            if (indexToRemove == -1) {
                val dashBoardAlertList = ArrayList<DashNotification>()
                if (!dashboardAlerts.value.isNullOrEmpty()) {
                    dashBoardAlertList.addAll(dashboardAlerts.value!!)
                }
                dashBoardAlertList.add(getBackgroundPermissionAlert())
                dashboardAlerts.postValue(dashBoardAlertList)
            }
        }
    }

    private fun checkBackgroundLocationPermission(): Boolean {
        val permissionAccessCoarseLocationApproved =
            (ActivityCompat.checkSelfPermission(
                NoiseFitApplicationMain.context!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)

        val backgroundLocationPermissionApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                (ActivityCompat.checkSelfPermission(
                    NoiseFitApplicationMain.context!!,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            } else {
                true
            }

        if (permissionAccessCoarseLocationApproved && backgroundLocationPermissionApproved) {
            return true
        }

        return false

    }

    private fun ignoreBatteryOptimizationForSomeManufacturer(): Boolean {
        val deviceMan = Build.MANUFACTURER.lowercase()
        LOGS.d("MANUFACTURER $deviceMan")
        if (deviceMan.contains("realme", false) || deviceMan.contains("oppo", false)) {
            return true
        }
        return false

    }

    fun removeDashboardAlert(position: Int) {
        tryCatch {
            val currentData = dashboardAlerts.value
            if (currentData != null && currentData.size > position) {
                (currentData as ArrayList).removeAt(position)
                dashboardAlerts.value = currentData
            }
        }

    }

    private fun showPromotionalBanner() {
        if (summary.connectedDevice == null) {
            return
        }

        if (!PromotionalUtil.showPromotionalBanner(summary.connectedDevice)) {
            return
        }
        val isPromotionalBannerShown = localDataStore.isPromotionalBannerShown()
        if (isPromotionalBannerShown) {
            return
        }
        localDataStore.setPromotionalBannerShown(true)
        summary.showPromotionalBanner.value = Event(true)
    }


    fun isDeviceConnected(): Boolean {
        if (getDeviceConnected() == null) {
            return false
        }

        if (sessionManager.connectState.value is ConnectState.ConnectSuccess) {
            return true
        }
        return false
    }

    val showNextImage = MutableLiveData<Event<Boolean>>()
    val showNextIplData = MutableLiveData<Event<Boolean>>()

    inner class RemindTask : TimerTask() {
        override fun run() {
            showNextImage.postValue(Event(true))
        }
    }

    inner class NplScrollTask : TimerTask() {
        override fun run() {
            showNextIplData.postValue(Event(true))
        }
    }

    private fun getGreetingMessage(): String {
        val currentTime = DateFormats.getTimeFormat()
        if (DateFormats.isTimeBetween(currentTime, "04:00", "11:59")) {
            return "Good morning"
        } else if (DateFormats.isTimeBetween(currentTime, "12:00", "16:59")) {
            return "Good afternoon"
        } else if (DateFormats.isTimeBetween(currentTime, "17:00", "20:59")) {
            return "Good evening"
        } else if (DateFormats.isTimeBetween(
                currentTime, "21:00", "23:59"
            ) || DateFormats.isTimeBetween(currentTime, "00:00", "03:59")
        ) {
            return "Hi"
        }

        return "Hi"
    }


}