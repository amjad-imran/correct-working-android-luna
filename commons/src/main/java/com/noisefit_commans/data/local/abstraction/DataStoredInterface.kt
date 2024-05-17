package com.noisefit_commans.data.local.abstraction


import com.noisefit.data.remote.response.CatWiseWatchFacesItem
import com.noisefit.data.remote.response.WatchFaceCustomListResponse
import com.noisefit_commans.data.enums.DashInfoCard
import com.noisefit_commans.data.enums.ServiceState
import com.noisefit_commans.data.model.*
import com.noisefit_commans.data.model.matches.Matches
import com.noisefit_commans.models.AppNotificationsSettings
import com.noisefit_commans.models.EnabledAppsForNotifications
import com.noisefit_commans.models.Location
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.models.Units
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.models.WatchFace


interface DataStoredInterface {

    fun getSleepNotificationTimeStamp(): Long
    fun setSleepNotificationTimeStamp()

    fun getReadinessNotificationTimeStamp(): Long
    fun setReadinessNotificationTimeStamp()

    fun updateUserToken(token: Token?)
    fun getUserToken(): Token?

    fun getIsBatteryAlertShown(): Boolean
    fun setBatteryAlertShown()
    fun clearUserLogoutData()

    @Deprecated("use updateUserToken()")
    fun updateDeviceToken(token: String?)

    @Deprecated("use getUserToken()")
    fun getDeviceToken(): String?
    fun updateLocations(location: Location)
    fun getLocation(): Location?

    fun saveSchedulerTimeAndFrequency(interval: Int, syncFrequency: Int)
    fun getSchedulerTimeInterval(): Int?
    fun getSyncIntervalFrequency(): Int?
    fun saveSyncTriedNumber(num: Int)
    fun getSyncTriedNumber(): Int?
    fun clearConnectedDevice()
    fun updateNotificationSettings(notificationSettings: AppNotificationsSettings?)
    fun getNotificationSettings(): AppNotificationsSettings?
    fun updateEnabledAppsForNotification(enabledAppsForNotifications: EnabledAppsForNotifications?)
    fun getEnabledAppsForNotification(): EnabledAppsForNotifications?
    fun updateWeatherSettings(status: Boolean)
    fun getWeatherSwitch(): Boolean?
    fun getPreferencesWatchLogsName(default_value: String?): String?
    fun saveUserInfo(user: User)
    fun deleteUserInfo()
    fun deleteUserToken()
    fun setFcmToken(token: String)
    fun getFcmToken(): String?
    fun deleteFcmToken()
    fun deleteAdditionalInfo()
    fun setUserDataSynced(status: Boolean)
    fun getUserDataSyncStatus(): Boolean
    fun getUser(): User?
    fun setServiceState(state: ServiceState)
    fun getServiceState(): ServiceState

    fun getUnit(): Units
    fun saveTemperatureUnit(unit: Units)
    fun getTemperatureUnit(): Units
    fun saveDeviceFeatures(deviceFeatures: DeviceFeatures)
    fun getDeviceFeatures(): DeviceFeatures?
    fun updateUserActivities(healthOverviewData: HealthOverviewData)
    fun getUserActivities(): HealthOverviewData?

    fun isCallAlertEnabled(): Boolean
    fun setCallAlertStatus(status: Boolean)
    fun isSMSAlertEnabled(): Boolean
    fun setSMSAlertStatus(status: Boolean)

    fun isNotificationAlertEnabled(): Boolean
    fun setNotificationAlertStatus(status: Boolean)
    fun saveNotificationAppList(app: List<NotificationApp>)
    fun removeNotificationApp(app: NotificationApp)
    fun clearNotificationAppList()
    fun clearOldNotificationAppList()
    fun getNotificationEnabledAppList(): List<NotificationApp>?
    fun getOldNotificationEnabledAppList(): List<NotificationApp>?
    fun getTimeFormat(): String?
    fun setTimeFormat(format: String)
    fun isEnableGoogleFit(): Boolean
    fun setGoogleFitStatus(status: Boolean)

    fun setVerifyMobileNumberStatus(status: Boolean)
    fun getVerifyMobileNumberStatus(): Boolean
    fun setChallengeInfo(data: String)
    fun getChallengeInfo(): String?
    fun saveLastWatchFace(watchFace: WatchFace)
    fun getLastWatchFace(): WatchFace?
    fun clearLastWatchFace()
    fun setWarrantyStatus(warrantyStatus: Int)
    fun getWarrantyStatus(): Int

    fun saveLastSyncTimeStamp(timeStamp: Long)
    fun getLastSyncTimeStamp(): Long?

    fun saveLastWeatherSyncTimeStamp(timeStamp: Long)
    fun getLastWeatherSyncTimeStamp(): Long

    fun saveCrashLog(crashlog: String)
    fun getCrashLog(): String?
    fun isAutoStartEnabled(): Boolean
    fun setAutoStart(b: Boolean)

    fun setIgnoreVersion(number: Int)
    fun getIgnoreVersion(): Int


    fun setLastSyncWithServer(timeStamp: Long)
    fun getLastSyncWithServer(): Long

    fun setStepsLastSyncHash(hash: String)
    fun getStepsLastSyncHash(): String?

    fun setHeartLastSyncHash(hash: String)
    fun getHeartLastSyncHash(): String?

    fun setBodyTempSyncHash(hash: String)
    fun getBodyTempSyncHash(): String?

    fun setStressLastSyncHash(hash: String)
    fun getStressLastSyncHash(): String?

    fun setBloodOxygenLastSyncHash(hash: String)
    fun getBloodOxygenLastSyncHash(): String?

    fun setSleepLastSyncHash(hash: String)
    fun getSleepLastSyncHash(): String?


    fun saveAndGetLocation(data: List<LocationDataModel>): List<LocationDataModel>
    fun clearLocation()
    fun incrementAppOpenCount()
    fun getAppOpenCount(): Int

    fun get80NotificationTimeStamp(): Long
    fun getNotificationCompleteTimeStamp(): Long

    fun clearNotificationGoalTimeStamp()

    fun get80NotificationStatus(): Boolean
    fun set80NotificationStatus(status: Boolean)
    fun getGoalCompleteNotificationStatus(): Boolean
    fun setGoalCompleteNotificationStatus(status: Boolean)
    fun getNotificationMessageClearStatus(): Boolean
    fun setNotificationMessageClearedStatus(b: Boolean)

    fun getPrivacyPolicyStatus(): Boolean
    fun setPrivacyPolicyStatus(b: Boolean)
    fun getNplPrivacyPolicyStatus(): Boolean
    fun setNplPrivacyPolicyStatus(b: Boolean)

    fun setBodyTempUnit(unit: Units)
    fun getBodyTempUnit(): Units

    fun setBluetoothDialogShown(status: Boolean)
    fun isBluetoothDialogShown(): Boolean


    fun getDeviceFeaturesLastSyncTime(): Long
    fun setDeviceFeaturesLastSyncTime()

    /**
     * Save in hours
     */
    fun saveFeatureIntervalFetchPeriod(resetInterval: Int)
    fun saveLogSyncInterval(resetInterval: Int)

    /**
     * Returns value in hours
     */
    fun getFeatureIntervalFetchPeriod(): Int
    fun getLogSyncInterval(): Int

    fun getRandomWatchFaceListSyncTime(): Long
    fun setRandomWatchFaceListSyncTime(timeStamp: Long)

    fun getRandomWatchFaceListResponse(): List<WatchFace>?
    fun setRandomWatchFaceListResponse(data: List<WatchFace>)
    fun clearRandomWatchFaceListResponse()


    fun getLastStepsSyncWithServer(): Long
    fun setLastStepsSyncWithServer(timeStamp: Long)

    fun getLastClearTables(): Long
    fun setLastClearTables(timeStamp: Long)

    /**
     * For Review Logic
     */
    fun setFirstOpenTimeStamp(timeStamp: Long)
    fun getFirstOpenTimeStamp(): Long

    fun setLastReviewShownTimeStamp(timeStamp: Long)
    fun getLastReviewShownTimeStamp(): Long

    fun setAwardCount(count: Int)
    fun getAwardCount(): Int
    fun setGoalCompletionCount(count: Int)
    fun getGoalCompletionCount(): Int
    fun setWorkoutShareCount(count: Int)
    fun getWorkoutShareCount(): Int
    fun setWatchFaceTransferCount(count: Int)
    fun getWatchFaceTransferCount(): Int
    fun setCustomWatchFaceTransferCount(count: Int)
    fun getCustomWatchFaceTransferCount(): Int

    fun saveLastSportApiTimeStamp(timeStamp: Long)
    fun getLastSportApiTimeStamp(): Long

    fun isEnableIpl(): Boolean
    fun setIplStatus(status: Boolean)

    fun getSportEvenApiCallInterval(): Int
    fun setSportEvenApiCallInterval(data: Int)//in minutes

    fun getCacheSportEventList(): Matches?
    fun setCacheSportEventList(data: Matches)

    fun set25DaysReviewShown(b: Boolean)
    fun is25DaysReviewShown(): Boolean

    fun setLastMatchSelected(date: String)
    fun getLastMatchSelected(): String?

    fun setEndGameList(list: ArrayList<EndGame>)
    fun getEndGameList(): ArrayList<EndGame>

    fun setEndGameValue(list: String)
    fun getEndGameValue(): String

    fun setPairLaterClicked(b: Boolean)
    fun getPairLaterStatus(): Boolean

    fun saveWatchUpdateLogs(logText: String)
    fun getWatchUpdateLogs(): String

    fun setEditHealthOverView(editHealthOverView: EditHealthOverView)
    fun getEditHealthOverView(): EditHealthOverView

    fun getLastPeriodicDataSyncTime(): Long
    fun setLastPeriodicDataSyncTime(timeStamp: Long)

    fun getLastInfoFetchTime(): Long
    fun setLastInfoFetchTime(timeStamp: Long)

    fun getLastBannerApiCallFetchTime(): Long
    fun setLastBannerApiCallFetchTime(timeStamp: Long)

    fun setBannerList(list: ArrayList<DashboardBanner>)
    fun getBannerList(): ArrayList<DashboardBanner>

    fun showEnableBgPermissionDialog(status: Boolean)
    fun getEnableBgPermissionDialog(): Boolean

    fun getLocalUserData(): LocalUserData?
    fun setLocalUserData(localUserData: LocalUserData?)

    /**
     * 0->No value stored
     * 1-> setup pending
     * 2->Setup Done
     */
    fun setDeviceSetupStatus(status: Int)
    fun getDeviceSetupStatus(): Int

    fun setBatteryOptimisationStatus(status: Boolean)
    fun getBatteryOptimisationStatus(): Boolean

    fun setAlertConnectivityStatus(status: Boolean)
    fun getAlertConnectivityStatus(): Boolean

    fun setAlertSupportStatus(status: Boolean)
    fun getAlertSupportStatus(): Boolean

    fun isPromotionalBannerShown(): Boolean
    fun setPromotionalBannerShown(status: Boolean)

    fun isDefaultNotificationAdded(): Boolean
    fun setDefaultNotificationAdded(status: Boolean)

    fun getCustomWatchFaceData(): WatchFaceCustomListResponse?
    fun setCustomWatchFaceData(data: WatchFaceCustomListResponse?)

    fun getRecentActivities(): RecentActivities?
    fun setRecentActivities(data: RecentActivities?)

    fun getDashboardBanners(): DashboardBannerData?
    fun setDashboardBanners(data: DashboardBannerData?)

    fun getRoundUpData(): RoundUpResponse?
    fun setRoundUpData(data: RoundUpResponse?)

    fun getWorkoutImages(): List<String>?
    fun setWorkoutImages(data: List<String>?)

    fun getWatchFaceCategories(): List<CatWiseWatchFacesItem>?
    fun setWatchFaceCategories(data: List<CatWiseWatchFacesItem>?)

    fun saveHistoryYears(i: Int)
    fun getHistoryYears(): Int
    fun isChallengeHelperScreenClick(): Boolean
    fun setChallengeHelperScreenClick(b: Boolean)

    fun getExperimentalSettings(): ExperimentalSettings
    fun setExperimentalSettings(data: ExperimentalSettings)


    fun getIsTestModeOn(): Boolean
    fun setIsTestModeOn(isTestModeOn: Boolean)
    fun clearInterestStatus()
    fun setInterestCancelled()
    fun isInterestCancelled(): Boolean

    fun setFriendsWalkAround(status: Boolean)
    fun isFriendsWalkAround(): Boolean

    fun setFriendsPrivacyAcceptStatus(status: Boolean)
    fun getFriendsPrivacyAcceptStatus(): Boolean

    fun setDiyWalkAround(status: Boolean)
    fun isDiyWalkAround(): Boolean

    fun setCoinsWalkAround(status: Boolean)
    fun isCoinsWalkAroundShown(): Boolean

    fun setUserLocationMapped(status: Boolean)
    fun isUserLocationMapped(): Boolean

    fun getBatteryNotification(): BatteryNotification?
    fun setBatteryNotification(data: BatteryNotification)

    fun getChargeOverNightNotification(): Boolean
    fun setChargeOverNightNotification(data: Boolean)


    fun getLastBatteryNotificationState(): BatteryNotificationLastStateData?
    fun setLastBatteryNotificationState(data: BatteryNotificationLastStateData?)

    fun getNoiseFitContactCount(): Int
    fun setNoiseFitContactCount(int: Int)

    fun getYearlyEndGoal(): String?
    fun setYearlyEndGoal(goal: String)
    fun deleteYearlyGoal()

    fun isHoldPressPopEmojiViewShown(): Boolean
    fun setHoldPressPopEmojiViewShown(data: Boolean)

    fun getAGPSStatusState(): AGPSStatusState?
    fun setAGPSStatusState(data: AGPSStatusState)

    fun setActivitySyncCount(count: Int)
    fun getActivitySyncCount(): Int

    fun setJoinedChallengeCount(count: Int)
    fun getJoinedChallengeCount(): Int

    fun setFriendCount(count: Int)
    fun getFriendCount(): Int

    fun setQrCodeInfoStatus(b: Boolean)
    fun getQrCodeInfoStatus(): Boolean

    fun setIsWatchFaceRewardEarned(b: Boolean)
    fun getIsWatchFaceRewardEarned(): Boolean

    fun setSleepLastLocalNotification(date: String)
    fun getSleepLastLocalNotification(): String?

    fun getTimeLineCurrentPageCount(): Int
    fun setTimeLineCurrentPageCount(page: Int)

    fun setNplWalkAround(status: Boolean)
    fun isNplWalkAroundShown(): Boolean

    fun getLastWinsCount(): Int
    fun setLastWinsCount(winCount: Int)

    fun setQuizQuestion(quizData: NplQuizDataModel)
    fun getQuizQuestion(): NplQuizDataModel?

    fun clearQuizQuestionData()

    fun setQuizApiCallTimeStamp(timeStamp: Long)
    fun getQuizApiCallTimeStamp(): Long
    fun setFeedPostCreateCount(count: Int)
    fun getFeedPostCreateCount(): Int

    fun setLaterNowLastReviewShownTimeStamp(timeStamp: Long)
    fun getLaterNowLastReviewShownTimeStamp(): Long

    fun setTenDayLaterNowLasTimeStamp(timeStamp: Long)
    fun getTenDayLaterNowLastTimeStamp(): Long

    fun setShowReviewPopUp(boolean: Boolean)
    fun isShowReviewPopUp(): Boolean

    fun setWatchFaceRatedId(id: Int)
    fun clearWatchFaceRatedId(id: Int)
    fun checkWatchFaceRatedIdExist(id: Int): Boolean

    fun saveLastTokenRefreshTimestamp()
    fun getLastTokenRefreshTimestamp(): Long

    fun isPreviouslyPaired(): Boolean
    fun setPreviouslyPaired()

    fun getDashCardClickState(): HashMap<DashInfoCard, Boolean>
    fun clearDashCardClickState()
    fun setDashCardClickState(type: DashInfoCard, boolean: Boolean)

    fun getGFitUserDataLastSyncTime(): Long
    fun setGFitUserDataLastSyncTime()

    fun getUserHealthCacheVersion(): Int
    fun setUserHealthCacheVersion(version: Int)


    fun saveNewAppVersion(newAppData: String, currentVersion: Int)
    fun saveAppVersionCheckTimeStamp()
    fun getAppVersionCheckTimeStamp(): Long
    fun getNewAppVersion(): Triple<String, Int, Long>?
    fun cleaNewAppVersion()
    fun saveAppRemindDate()
    fun getAppRemindDate(): String?

    fun isNewAppVersionAvailable(): Boolean
    fun setStressWalkthroughShown(isShown: Boolean)

    fun getStressWalkthroughShownStatus(): Boolean

    fun isAiChatSplashShown(): Boolean
    fun setAiChatSplashShown()
}