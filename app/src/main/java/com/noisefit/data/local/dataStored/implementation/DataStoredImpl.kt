package com.noisefit.data.local.dataStored.implementation


import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit.data.remote.response.CatWiseWatchFacesItem
import com.noisefit.data.remote.response.WatchFaceCustomListResponse
import com.noisefit_commans.data.enums.DashInfoCard
import com.noisefit_commans.data.enums.ServiceState
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.DashboardBanner
import com.noisefit_commans.data.model.DashboardBannerData
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.data.model.EditHealthOverView
import com.noisefit_commans.data.model.EndGame
import com.noisefit_commans.data.model.ExperimentalSettings
import com.noisefit_commans.data.model.HealthOverviewData
import com.noisefit_commans.data.model.LocalUserData
import com.noisefit_commans.data.model.NotificationApp
import com.noisefit_commans.data.model.NplQuizDataModel
import com.noisefit_commans.data.model.RecentActivities
import com.noisefit_commans.data.model.RoundUpResponse
import com.noisefit_commans.data.model.Token
import com.noisefit_commans.data.model.User
import com.noisefit_commans.data.model.matches.Matches
import com.noisefit_commans.models.AppNotificationsSettings
import com.noisefit_commans.models.EnabledAppsForNotifications
import com.noisefit_commans.models.Location
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.models.TimeFormats
import com.noisefit_commans.models.Units
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.utils.DateFormats
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val LAST_BANNER_API_FETCH_TIME = "LAST_BANNER_API_FETCH_TIME"
private const val BANNER_LIST = "BANNER_LIST"
private const val LAST_INFO_FETCH_TIME = "LAST_INFO_FETCH_TIME"
private const val AUTO_START_FLAG = "AUTO_START_FLAG"
private const val CRASH_LOG = "CRASH_LOG"
private const val USER_TOKEN = "user_token_ref"
private const val DEVICE_TOKEN = "device_token"
private const val CHALLENGE_INFO = "CHALLENGE_INFO"
private const val WARRANTY_STATUS = "WARRANTY_STATUS_2"
private const val LAST_SYNC = "LAST_SYNC"
private const val LAST_SYNC_WEATHER = "LAST_SYNC_WEATHER"
private const val SYNC_USER_INFO = "SYNC_USER_INFO"
private const val USER_INFO = "user_info"
private const val USER_INFO_SYNCED = "USER_INFO_SYNCED"
private const val USER_PROFILE = "user_profile"
private const val FCM_LAST_TOKEN = "FCM_LAST_TOKEN"
private const val LAST_WATCHFACE = "LAST_WATCHFACE"
private const val USER_GOALS = "user_goals"
private const val USER_UNIT = "user_unit"
private const val TEMPERATURE_UNIT = "TEMPERATURE_UNIT"
private const val DEVICE_FEATURES = "device_features"
private const val ENABLED_NOTIFICATION_APP = "ENABLED_NOTIFICATION_APP"
private const val NEW_ENABLED_NOTIFICATION_APP = "NEW_ENABLED_NOTIFICATION_APP"
private const val NOTIFICATION_ALERT_STATUS = "NOTIFICATION_ALERT_STATUS"
private const val LOCATIONS = "locations"
private const val NOISE_FIT_DEVICE = "noise_fit_device"
private const val SCHEDULER_TIME_INTERVAL = "scheduler_time_interval"
private const val SYNC_INTERVAL_FREQUENCY = "sync_interval_frequency"
private const val SYNC_TRIED_NUMBER = "sync_tried_number"
private const val WEATHER_SWITCH = "weather_switch"
private const val TIME_FORMAT = "TIME_FORMAT"
private const val NOTIFICATION_SETTINGS = "notification_settings"
private const val ENABLED_APPS_FOR_NOTIFICATION = "enabledAppsForNotification"
private const val WATCH_LOGS_NAME = "watch_logs_name"
private const val CONNECTION_SERVICE_STATE = "CONNECTION_SERVICE_STATE"
private const val USER_ACTIVITIES = "user_activities"
private const val GOOGLE_FIT_STATUS = "google_fit_status"
private const val VERIFY_MOBILE_NUMBER = "verify_mobile_number"
private const val IGNORE_VERSION_NUMBER = "IGNORE_VERSION_NUMBER"
private const val LAST_SYNC_WITH_SERVER = "LAST_SYNC_WITH_SERVER"

private const val STEPS_LAST_SYNC_HASH = "STEPS_LAST_SYNC_HASH"
private const val HEART_LAST_SYNC_HASH = "HEART_LAST_SYNC_HASH"
private const val BLOOD_LAST_SYNC_HASH = "BLOOD_LAST_SYNC_HASH"
private const val STRESS_LAST_SYNC_HASH = "STRESS_LAST_SYNC_HASH"
private const val SLEEP_LAST_SYNC_HASH = "SLEEP_LAST_SYNC_HASH"
private const val BODY_TEMP_LAST_SYNC_HASH = "BODY_TEMP_LAST_SYNC_HASH"
private const val MAPS_LAT_LONG = "MAPS_LAT_LONG"
private const val APP_OPEN_COUNT = "APP_OPEN_COUNT"
private const val NOTIFICATION_80_STATUS = "NOTIFICATION_80_STATUS"
private const val NOTIFICATION_80_STATUS_TIME = "NOTIFICATION_80_STATUS_TIME"
private const val DEVICE_FEATURE_SYNC_TIME = "DEVICE_FEATURE_SYNC_TIME_1"
private const val NOTIFICATION_GOAL_STATUS = "NOTIFICATION_GOAL_STATUS"
private const val NOTIFICATION_GOAL_STATUS_TIME = "NOTIFICATION_GOAL_STATUS_TIME"
private const val NOTIFICATION_SUMMARY_CLEAR_STATUS = "NOTIFICATION_SUMMARY_CLEAR_STATUS"
private const val WATCH_FACE_CATEGORY_RESPONSE = "WATCH_FACE_CATEGORY_RESPONSE"
private const val PRIVACY_POLICY_STATUS = "PRIVACY_POLICY_STATUS"
private const val NPL_PRIVACY_POLICY_STATUS = "NPL_PRIVACY_POLICY_STATUS_3"
private const val BODY_TEMP_UNIT = "BODY_TEMP_UNIT"
private const val BLUETOOTH_ENABLE_DIALOG = "BLUETOOTH_ENABLE_DIALOG1"
private const val FEATURE_REFETCH_PERIOD = "FEATURE_REFETCH_PERIOD"
private const val LOGS_SYNC_INTERVAL = "LOGS_SYNC_INTERVAL"
private const val PERIODIC_DATA_TIMESTAMP = "PERIODIC_DATA_TIMESTAMP"
private const val STEPS_LAST_SYNC_WITH_SERVER = "STEPS_LAST_SYNC_WITH_SERVER_TIMESTAMP"
private const val LAST_CLEAR_TABLE_TIMESTAMP = "LAST_CLEAR_TABLE_TIMESTAMP"
private const val FIRST_OPEN_TIMESTAMP = "FIRST_OPEN_TIMESTAMP"
private const val REVIEW_SHOWN_TIMESTAMP = "REVIEW_SHOWN_TIMESTAMP"
private const val AWARD_COUNT = "AWARD_COUNT"
private const val GOAL_COMPLETION_COUNT = "GOAL_COMPLETION_COUNT"
private const val WORKOUT_SHARE_COUNT = "WORKOUT_SHARE_COUNT"
private const val WATCHFACE_TRANSFER_COUNT = "WATCHFACE_TRANSFER_COUNT"
private const val WATCHFACE_TRANSFER_CUSTOM_COUNT = "WATCHFACE_TRANSFER_CUSTOM_COUNT"
private const val MATCH_INFO_TIMESTAMP = "MATCH_INFO_TIMESTAMP1"
private const val IPL_2022_STATE = "IPL_2022_STATE"
private const val SPORT_EVENT_INTERVAL_TIME = "SPORT_EVENT_INTERVAL_TIME"
private const val SPORT_EVENT_CACHE_LIST = "SPORT_EVENT_CACHE_LIST"
private const val REVIEW_25_DAYS_STATUS = "REVIEW_25_DAYS_STATUS"
private const val LAST_MATCH_SELECTED_EVENT = "LAST_MATCH_SELECTED_EVENT"
private const val PAIR_LATER = "PAIR_LATER"
private const val WATCH_UPDATE_LOGS = "WATCH_UPDATE_LOGS"
private const val ENABLE_BG_PERMISSION = "ENABLE_BG_PERMISSION"
private const val EDIT_DASHBOARD_LIST = "EDIT_DASHBOARD_LIST1"
private const val END_GAME_LIST = "END_GAME_LIST"
private const val END_GAME_VALUE = "END_GAME_VALUE"
private const val DEVICE_SETUP_STATUS_NEW = "DEVICE_SETUP_STATUS_NEW"
private const val NPL_WINS_COUNT = "NPL_WINS_COUNT"

private const val LOCAL_USER_DATA = "LOCAL_USER_DATA"
private const val RANDOM_WATCH_FACE_LIST = "RANDOM_WATCH_FACE_LIST_2"
private const val RANDOM_WATCH_FACE_SYNC_TIME = "RANDOM_WATCH_FACE_SYNC_TIME_2"

private const val WATCHFACE_MAIN_LIST_DATA = "WATCHFACE_MAIN_LIST_DATA"
private const val WATCHFACE_CATEGORIES = "WATCHFACE_CATEGORIES"
private const val RECENT_ACTIVITIES_LIST = "RECENT_ACTIVITIES_LIST"

private const val HISTORY_YEARS = "HISTORY_YEARS"
private const val DASHBOARD_BANNERS_1 = "DASHBOARD_BANNERS_1"
private const val ROUND_UP_DATA = "ROUND_UP_DATA"
private const val IS_PREVIOUSLY_PAIRED = "IS_PREVIOUSLY_PAIRED"
private const val USER_HEALTH_CACHE_V = "USER_HEALTH_CACHE_V"
private const val WORKOUT_IMAGES = "WORKOUT_IMAGES"


private const val CALL_ALERT = "CALL_ALERT"
private const val SMS_ALERT = "SMS_ALERT"

private const val BATTERY_OPTIMISATION_STATUS = "BATTERY_OPTIMISATION_STATUS"
private const val ALERT_CONNECTIVITY_STATUS = "ALERT_CONNECTIVITY_STATUS"
private const val ALERT_SUPPORT_STATUS = "ALERT_SUPPORT_STATUS"

private const val PROMITONAL_BANNER_STATE = "PROMITONAL_BANNER_STATE"
private const val ADDED_RECOMMENDED_NOTIFICATION = "ADDED_RECOMMENDED_NOTIFICATION"
private const val CHALLENGE_HELPER_CLICK = "CHALLENGE_HELPER_CLICK"

private const val EXPERIMENTAL_SETTINGS_KEY = "EXPERIMENTAL_SETTINGS_KEY"
private const val TEST_MODE_ON_KEY = "TEST_MODE_ON_KEY"
private const val IS_INTEREST_CANCELLED = "IS_INTEREST_CANCELLED"
private const val FRIENDS_WALKAROUND_KEY = "FRIENDS_WALKAROUND_KEY"
private const val FRIENDS_TERMS_KEY = "FRIENDS_TERMS_KEY"
private const val COINS_WALKAROUND_KEY = "COINS_WALKAROUND_KEY"
private const val NPL_WALKAROUND_KEY = "NPL_WALKAROUND_KEY"
private const val USER_LOCATION_MAPPED_KEY = "USER_LOCATION_MAPPED_KEY"

private const val BATTERY_NOTIFICATION_KEY = "BATTERY_NOTIFICATION_KEY"
private const val CHARGE_O_NIGHT_NOTIFICATION_KEY = "CHARGE_O_NIGHT_NOTIFICATION_KEY"

private const val BATTERY_NOTIFICATION_LAST_STATE_KEY = "BATTERY_NOTIFICATION_LAST_STATE_KEY"

private const val DEMO_EMOJI_POPUP_KEY = "DEMO_EMOJI_POPUP_KEY"
private const val NOISE_FIT_CONTACT_COUNT = "NOISE_FIT_CONTACT_COUNT"
private const val YEAR_END_GOAL = "YEAR_END_GOAL"
private const val AGPS_STATE_KEY = "AGPS_STATE_KEY"
private const val ACTIVITY_SYNC_COUNT = "ACTIVITY_SYNC_COUNT"
private const val JOINED_CHALLENGE_COUNT = "JOINED_CHALLENGE_COUNT"
private const val FRIEND_COUNT = "FRIEND_COUNT"
private const val QR_INFO_STATUS = "QR_INFO_STATUS"
private const val WF_REWARDS = "WF_REWARDS"
private const val SLEEP_LOCAL_NOTIFICATION_KEY = "SLEEP_LOCAL_NOTIFICATION_KEY"
private const val QUIZ_QUESTION_DATA = "QUIZ_QUESTION_DATA"
private const val QUIZ_API_CALL_TIME = "QUIZ_API_CALL_TIME"
private const val SUBMIT_ANSWER_DATA_FORCE_KILL_CHECK = "SUBMIT_ANSWER_DATA_FORCE_KILL_CHECK"
private const val TIMELINE_PAGE_COUNT = "TIMELINE_PAGE_COUNT"
private const val DIY_WALK_AROUND_KEY = "DIY_WALK_AROUND_KEY"
private const val FEEDS_POST_COUNT = "FEEDS_POST_COUNT"
private const val TEN_DAY_REVIEW_SHOWN_TIMESTAMP = "TEN_DAY_REVIEW_SHOWN_TIMESTAMP"
private const val LATER_NOW_REVIEW_SHOWN_TIMESTAMP = "LATER_NOW_REVIEW_SHOWN_TIMESTAMP"
private const val SHOW_REVIEW_POP_UP = "SHOW_REVIEW_POP_UP"
private const val PAIR_DEVICE_TYPE = "PAIR_DEVICE_TYPE"
private const val WF_RATING_KEY = "WF_RATING_KEY"
private const val TOKEN_LAST_UPDATE = "TOKEN_LAST_UPDATE"
private const val DASH_CARD_CLICK_STATE = "DASH_CARD_CLICK_STATE"
private const val BATTERY_DASH_ALERT = "BATTERY_DASH_ALERT"
private const val SLEEP_NOTIFICATION = "SLEEP_NOTIFICATION"
private const val READINESS_NOTIFICATION = "READINESS_NOTIFICATION"
private const val STRESS_WALKRHTOUGH = "STRESS_WALKRHTOUGH"
private const val FMH_WALK_THROUGH = "FMH_WALK_THROUGH"
private const val FMH_REMIND_LATER = "FMH_REMIND_LATER"
private const val SLEEP_MOENGAGE_SYNC_DATE = "SLEEP_MOENGAGE_SYNC_DATE"
private const val AI_CHAT_ONBOARD = "AI_CHAT_ONBOARD"


private const val APP_VERSION_NEW = "APP_VERSION_NEW"
private const val APP_VERSION_NEW_TIMESTAMP = "APP_VERSION_NEW_TIMESTAMP"
private const val APP_VERSION_REMIND = "APP_VERSION_REMIND"
private const val APP_VERSION_CURRENT = "APP_VERSION_CURRENT"

private const val GFIT_USER_SYNC_KEY = "GFIT_USER_SYNC_KEY"

private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class DataStoredImpl
@Inject constructor(
    private val gson: Gson, private val mPrefs: SharedPreferences
) : DataStoredInterface {

    //-1 if no value saved else days
    override fun getFMHWalkthroughRemindLaterDays(): Long {
        val savedTimeStamp = mPrefs.getLong(FMH_REMIND_LATER, -1)
        if (savedTimeStamp == -1L) {
            return 8
        }
        val currentTimeStamp = System.currentTimeMillis()
        val diffInMillis: Long = Math.abs(currentTimeStamp - savedTimeStamp)
        return TimeUnit.MILLISECONDS.toDays(diffInMillis)
    }

    override fun setFMHRemindLater() {
        mPrefs.edit()?.putLong(FMH_REMIND_LATER, System.currentTimeMillis())?.commit()
    }

    override fun getFMHWalkthroughShownStatus(): Boolean {
        return mPrefs.getBoolean(FMH_WALK_THROUGH, false)
    }

    override fun setFMHWalkthroughShown(isShown: Boolean) {
        mPrefs.edit()?.putBoolean(FMH_WALK_THROUGH, isShown)?.commit()
    }

    override fun saveSleepSyncedForDate(date: String) {
        mPrefs.edit()?.putString(SLEEP_MOENGAGE_SYNC_DATE, date)?.commit()
    }

    override fun isSleepSyncedForDate(date: String): Boolean {
        val savedDate = mPrefs.getString(SLEEP_MOENGAGE_SYNC_DATE, null) ?: return false
        return savedDate.equals(date)
    }

    override fun isAiChatSplashShown(): Boolean {
        return mPrefs.getBoolean(AI_CHAT_ONBOARD, false)
    }

    override fun setAiChatSplashShown() {
        mPrefs.edit()?.putBoolean(AI_CHAT_ONBOARD, true)?.commit()
    }

    override fun getStressWalkthroughShownStatus(): Boolean {
        return mPrefs.getBoolean(STRESS_WALKRHTOUGH, false)
    }

    override fun setStressWalkthroughShown(isShown: Boolean) {
        mPrefs.edit()?.putBoolean(STRESS_WALKRHTOUGH, isShown)?.commit()
    }

    override fun saveNewAppVersion(newAppData: String, currentVersion: Int) {
        mPrefs.edit()?.putString(APP_VERSION_NEW, newAppData)?.commit()
        mPrefs.edit()?.putInt(APP_VERSION_CURRENT, currentVersion)?.commit()
        saveAppVersionCheckTimeStamp()
    }

    override fun saveAppVersionCheckTimeStamp() {
        mPrefs.edit()?.putLong(APP_VERSION_NEW_TIMESTAMP, System.currentTimeMillis())?.commit()
    }

    override fun getAppVersionCheckTimeStamp(): Long {
        return mPrefs.getLong(APP_VERSION_NEW_TIMESTAMP, 0)
    }

    /**
     * update data
     * check version
     * timestamp of server check
     */
    override fun getNewAppVersion(): Triple<String, Int, Long>? {
        val gson = mPrefs.getString(APP_VERSION_NEW, null)
        return if (gson == null) {
            null
        } else {
            val timestamp = mPrefs.getLong(APP_VERSION_NEW_TIMESTAMP, 0L)
            Triple(
                gson,
                mPrefs.getInt(APP_VERSION_CURRENT, -1),
                timestamp
            )
        }
    }

    override fun cleaNewAppVersion() {
        mPrefs.edit()?.remove(APP_VERSION_NEW)?.commit()
        mPrefs.edit()?.remove(APP_VERSION_CURRENT)?.commit()
        mPrefs.edit()?.remove(APP_VERSION_REMIND)?.commit()
        mPrefs.edit()?.remove(APP_VERSION_NEW_TIMESTAMP)?.commit()
    }

    override fun saveAppRemindDate() {
        mPrefs.edit()?.putString(APP_VERSION_REMIND, DateFormats.getCurrentDate())?.commit()
    }

    override fun getAppRemindDate(): String? {
        return mPrefs.getString(APP_VERSION_REMIND, null)
    }

    override fun isNewAppVersionAvailable(): Boolean {
        return getNewAppVersion()?.first != null
    }

    override fun getReadinessNotificationTimeStamp(): Long {
        return mPrefs.getLong(READINESS_NOTIFICATION, 0)
    }

    override fun setReadinessNotificationTimeStamp() {
        mPrefs.edit()?.putLong(READINESS_NOTIFICATION, System.currentTimeMillis())?.commit()
    }

    override fun getSleepNotificationTimeStamp(): Long {
        return mPrefs.getLong(SLEEP_NOTIFICATION, 0)
    }

    override fun setSleepNotificationTimeStamp() {
        mPrefs.edit()?.putLong(SLEEP_NOTIFICATION, System.currentTimeMillis())?.commit()
    }

    override fun getIsBatteryAlertShown(): Boolean {
        return mPrefs.getBoolean(BATTERY_DASH_ALERT, false)
    }

    override fun setBatteryAlertShown() {
        mPrefs.edit()
            ?.putBoolean(BATTERY_DASH_ALERT, true)
            ?.commit()
    }

    override fun clearUserLogoutData() {
        mPrefs.edit()?.remove(BATTERY_DASH_ALERT)?.apply()
        mPrefs.edit()?.remove(STRESS_WALKRHTOUGH)?.apply()
        mPrefs.edit()?.remove(FMH_WALK_THROUGH)?.apply()
        mPrefs.edit()?.remove(FMH_REMIND_LATER)?.apply()
        mPrefs.edit()?.remove(AI_CHAT_ONBOARD)?.apply()
    }

    override fun getDashCardClickState(): HashMap<DashInfoCard, Boolean> {
        val data = mPrefs.getString(DASH_CARD_CLICK_STATE, null)
            ?: return HashMap<DashInfoCard, Boolean>().apply {
                this[DashInfoCard.ACTIVITY] = false
                this[DashInfoCard.READINESS] = false
                this[DashInfoCard.SLEEP] = false
                this[DashInfoCard.CARE] = false
                this[DashInfoCard.WELCOME] = false
            }
        return Gson().fromJson<HashMap<DashInfoCard, Boolean>>(data)
    }

    override fun clearDashCardClickState() {
        mPrefs.edit()?.remove(DASH_CARD_CLICK_STATE)?.apply()
    }

    override fun setDashCardClickState(type: DashInfoCard, boolean: Boolean) {
        val lastData = getDashCardClickState()
        lastData[type] = boolean
        mPrefs.edit()?.putString(DASH_CARD_CLICK_STATE, gson.toJson(lastData))?.commit()
    }

    override fun getGFitUserDataLastSyncTime(): Long {
        return mPrefs.getLong(GFIT_USER_SYNC_KEY, 0)
    }

    override fun setGFitUserDataLastSyncTime() {
        mPrefs.edit()?.putLong(GFIT_USER_SYNC_KEY, System.currentTimeMillis())?.apply()
    }

    override fun getUserHealthCacheVersion(): Int {
        return mPrefs.getInt(USER_HEALTH_CACHE_V, 1)
    }

    override fun setUserHealthCacheVersion(version: Int) {
        mPrefs.edit()?.putInt(USER_HEALTH_CACHE_V, version)?.apply()
    }

    override fun isPreviouslyPaired(): Boolean {
        return mPrefs.getBoolean(IS_PREVIOUSLY_PAIRED, false)
    }

    override fun setPreviouslyPaired() {
        mPrefs.edit()?.putBoolean(IS_PREVIOUSLY_PAIRED, true)?.apply()
    }

    override fun getDashboardBanners(): DashboardBannerData? {
        return mPrefs.getString(DASHBOARD_BANNERS_1, null)
            ?.let { Gson().fromJson<DashboardBannerData>(it) }
    }

    override fun getRoundUpData(): RoundUpResponse? {
        return mPrefs.getString(ROUND_UP_DATA, null)?.let { Gson().fromJson<RoundUpResponse>(it) }
    }

    override fun setRoundUpData(data: RoundUpResponse?) {
        mPrefs.edit()?.putString(ROUND_UP_DATA, gson.toJson(data))?.apply()
    }

    override fun getWorkoutImages(): List<String>? {

        return mPrefs.getString(WORKOUT_IMAGES, null)?.let { Gson().fromJson<List<String>>(it) }
    }

    override fun setWorkoutImages(data: List<String>?) {
        mPrefs.edit()?.putString(WORKOUT_IMAGES, gson.toJson(data))?.apply()
    }

    override fun setDashboardBanners(data: DashboardBannerData?) {
        mPrefs.edit()?.putString(DASHBOARD_BANNERS_1, gson.toJson(data))?.apply()
    }

    override fun saveHistoryYears(years: Int) {
        mPrefs.edit()?.putInt(HISTORY_YEARS, years)?.commit()
    }

    override fun getHistoryYears(): Int {
        return mPrefs.getInt(HISTORY_YEARS, 2)
    }

    override fun isChallengeHelperScreenClick(): Boolean {
        return mPrefs.getBoolean(CHALLENGE_HELPER_CLICK, false)
    }

    override fun setChallengeHelperScreenClick(b: Boolean) {
        mPrefs.edit()?.putBoolean(CHALLENGE_HELPER_CLICK, b)?.apply()
    }

    override fun getExperimentalSettings(): ExperimentalSettings {
        return mPrefs.getString(EXPERIMENTAL_SETTINGS_KEY, null)
            ?.let { Gson().fromJson<ExperimentalSettings>(it) } ?: ExperimentalSettings()
    }

    override fun setExperimentalSettings(data: ExperimentalSettings) {
        mPrefs.edit()?.putString(EXPERIMENTAL_SETTINGS_KEY, gson.toJson(data))?.apply()
    }

    override fun getIsTestModeOn(): Boolean {
        return mPrefs.getBoolean(TEST_MODE_ON_KEY, false)
    }

    override fun setIsTestModeOn(isTestModeOn: Boolean) {
        mPrefs.edit()?.putBoolean(TEST_MODE_ON_KEY, isTestModeOn)?.apply()
    }

    override fun getBatteryNotification(): com.noisefit_commans.data.model.BatteryNotification? {
        return mPrefs.getString(BATTERY_NOTIFICATION_KEY, null)
            ?.let { Gson().fromJson<com.noisefit_commans.data.model.BatteryNotification>(it) }
    }


    override fun setBatteryNotification(data: com.noisefit_commans.data.model.BatteryNotification) {
        mPrefs.edit()?.putString(BATTERY_NOTIFICATION_KEY, gson.toJson(data))?.apply()
    }

    override fun getChargeOverNightNotification(): Boolean {
        return mPrefs.getBoolean(CHARGE_O_NIGHT_NOTIFICATION_KEY, false)
    }

    override fun setChargeOverNightNotification(data: Boolean) {
        mPrefs.edit()?.putBoolean(CHARGE_O_NIGHT_NOTIFICATION_KEY, data)?.apply()
    }

    override fun getLastBatteryNotificationState(): com.noisefit_commans.data.model.BatteryNotificationLastStateData? {
        return mPrefs.getString(BATTERY_NOTIFICATION_LAST_STATE_KEY, null)
            ?.let {
                Gson().fromJson<com.noisefit_commans.data.model.BatteryNotificationLastStateData>(
                    it
                )
            }
    }

    override fun setLastBatteryNotificationState(data: com.noisefit_commans.data.model.BatteryNotificationLastStateData?) {
        mPrefs.edit()?.putString(BATTERY_NOTIFICATION_LAST_STATE_KEY, gson.toJson(data))?.apply()
    }

    override fun clearInterestStatus() {
        mPrefs.edit().remove(IS_INTEREST_CANCELLED).apply()
    }

    override fun setInterestCancelled() {
        mPrefs.edit()?.putBoolean(IS_INTEREST_CANCELLED, true)?.apply()
    }

    override fun isInterestCancelled(): Boolean {
        return mPrefs.getBoolean(IS_INTEREST_CANCELLED, false)
    }

    override fun setFriendsWalkAround(status: Boolean) {
        mPrefs.edit()?.putBoolean(FRIENDS_WALKAROUND_KEY, status)?.apply()
    }

    override fun isFriendsWalkAround(): Boolean {
        return mPrefs.getBoolean(FRIENDS_WALKAROUND_KEY, false)
    }

    override fun setFriendsPrivacyAcceptStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(FRIENDS_TERMS_KEY, status)?.apply()
    }

    override fun getFriendsPrivacyAcceptStatus(): Boolean {
        return mPrefs.getBoolean(FRIENDS_TERMS_KEY, false)
    }

    override fun setDiyWalkAround(status: Boolean) {
        mPrefs.edit()?.putBoolean(DIY_WALK_AROUND_KEY, status)?.apply()
    }

    override fun isDiyWalkAround(): Boolean {
        return mPrefs.getBoolean(DIY_WALK_AROUND_KEY, false)
    }

    override fun setCoinsWalkAround(status: Boolean) {
        mPrefs.edit()?.putBoolean(COINS_WALKAROUND_KEY, status)?.apply()
    }

    override fun isCoinsWalkAroundShown(): Boolean {
        return mPrefs.getBoolean(COINS_WALKAROUND_KEY, false)
    }

    override fun setUserLocationMapped(status: Boolean) {
        mPrefs.edit()?.putBoolean(USER_LOCATION_MAPPED_KEY, status)?.apply()
    }

    override fun isUserLocationMapped(): Boolean {
        return mPrefs.getBoolean(USER_LOCATION_MAPPED_KEY, false)
    }


    override fun getRandomWatchFaceListSyncTime(): Long {
        return mPrefs.getLong(RANDOM_WATCH_FACE_SYNC_TIME, 0)
    }

    override fun setRandomWatchFaceListSyncTime(timeStamp: Long) {
        mPrefs.edit()?.putLong(RANDOM_WATCH_FACE_SYNC_TIME, timeStamp)?.apply()
    }

    override fun getRandomWatchFaceListResponse(): List<WatchFace>? {
        return mPrefs.getString(RANDOM_WATCH_FACE_LIST, null)
            ?.let { Gson().fromJson<ArrayList<WatchFace>>(it) } ?: ArrayList()
    }

    override fun setRandomWatchFaceListResponse(data: List<WatchFace>) {
        mPrefs.edit()?.putString(RANDOM_WATCH_FACE_LIST, gson.toJson(data))?.apply()
    }

    override fun clearRandomWatchFaceListResponse() {
        mPrefs.edit().remove(RANDOM_WATCH_FACE_LIST).apply()
        mPrefs.edit().remove(RANDOM_WATCH_FACE_SYNC_TIME).apply()
    }

    override fun setDeviceSetupStatus(status: Int) {
        mPrefs.edit()?.putInt(DEVICE_SETUP_STATUS_NEW, status)?.apply()
    }

    override fun getDeviceSetupStatus(): Int {
        return mPrefs.getInt(DEVICE_SETUP_STATUS_NEW, 0)
    }

    override fun setBatteryOptimisationStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(BATTERY_OPTIMISATION_STATUS, status)?.apply()
    }

    override fun getBatteryOptimisationStatus(): Boolean {
        return mPrefs.getBoolean(BATTERY_OPTIMISATION_STATUS, false)
    }

    override fun setAlertConnectivityStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(ALERT_CONNECTIVITY_STATUS, status)?.apply()
    }

    override fun getAlertConnectivityStatus(): Boolean {
        return mPrefs.getBoolean(ALERT_CONNECTIVITY_STATUS, false)
    }

    override fun setAlertSupportStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(ALERT_SUPPORT_STATUS, status)?.apply()
    }

    override fun getAlertSupportStatus(): Boolean {
        return mPrefs.getBoolean(ALERT_SUPPORT_STATUS, false)
    }

    override fun isPromotionalBannerShown(): Boolean {
        return mPrefs.getBoolean(PROMITONAL_BANNER_STATE, false)
    }

    override fun setPromotionalBannerShown(status: Boolean) {
        mPrefs.edit()?.putBoolean(PROMITONAL_BANNER_STATE, status)?.apply()
    }

    override fun isDefaultNotificationAdded(): Boolean {
        return mPrefs.getBoolean(ADDED_RECOMMENDED_NOTIFICATION, false)
    }

    override fun setDefaultNotificationAdded(status: Boolean) {
        mPrefs.edit()?.putBoolean(ADDED_RECOMMENDED_NOTIFICATION, status)?.apply()
    }

    override fun getCustomWatchFaceData(): WatchFaceCustomListResponse? {
        return mPrefs.getString(WATCHFACE_MAIN_LIST_DATA, null)
            ?.let { Gson().fromJson<WatchFaceCustomListResponse>(it) }
    }

    override fun setCustomWatchFaceData(data: WatchFaceCustomListResponse?) {
        mPrefs.edit()?.putString(WATCHFACE_MAIN_LIST_DATA, gson.toJson(data))?.apply()
    }

    override fun getRecentActivities(): RecentActivities? {
        return mPrefs.getString(RECENT_ACTIVITIES_LIST, null)
            ?.let { Gson().fromJson<RecentActivities>(it) }
    }

    override fun setRecentActivities(data: RecentActivities?) {
        mPrefs.edit()?.putString(RECENT_ACTIVITIES_LIST, gson.toJson(data))?.apply()
    }

    override fun getWatchFaceCategories(): List<CatWiseWatchFacesItem>? {
        return mPrefs.getString(WATCHFACE_CATEGORIES, null)
            ?.let { Gson().fromJson<List<CatWiseWatchFacesItem>>(it) }
    }

    override fun setWatchFaceCategories(data: List<CatWiseWatchFacesItem>?) {
        mPrefs.edit()?.putString(WATCHFACE_CATEGORIES, gson.toJson(data))?.apply()
    }

    override fun getLocalUserData(): LocalUserData? {
        val userJson = mPrefs.getString(LOCAL_USER_DATA, null)
        if (userJson.isNullOrEmpty()) return null
        return gson.fromJson(userJson, LocalUserData::class.java)
    }

    override fun setLocalUserData(localUserData: LocalUserData?) {
        if (localUserData == null) {
            mPrefs.edit().remove(LOCAL_USER_DATA).apply()
            return
        }
        mPrefs.edit()?.putString(LOCAL_USER_DATA, gson.toJson(localUserData))?.apply()
    }

    override fun setPairLaterClicked(b: Boolean) {
        mPrefs.edit()?.putBoolean(PAIR_LATER, b)?.apply()
    }

    override fun getPairLaterStatus(): Boolean {
        return mPrefs.getBoolean(PAIR_LATER, false)
    }

    override fun set25DaysReviewShown(b: Boolean) {
        mPrefs.edit()?.putBoolean(REVIEW_25_DAYS_STATUS, b)?.apply()
    }

    override fun is25DaysReviewShown(): Boolean {
        return mPrefs.getBoolean(REVIEW_25_DAYS_STATUS, false)
    }

    override fun setLastMatchSelected(date: String) {
        mPrefs.edit()?.putString(LAST_MATCH_SELECTED_EVENT, date)?.apply()
    }

    override fun getWatchUpdateLogs(): String {
        return mPrefs.getString(WATCH_UPDATE_LOGS, "") ?: ""
    }

    override fun setEditHealthOverView(editHealthOverView: EditHealthOverView) {
        mPrefs.edit()?.putString(EDIT_DASHBOARD_LIST, gson.toJson(editHealthOverView))?.apply()
    }

    override fun getEditHealthOverView(): EditHealthOverView {

        return gson.fromJson(
            mPrefs.getString(EDIT_DASHBOARD_LIST, null), EditHealthOverView::class.java
        ) ?: return EditHealthOverView()
    }

    override fun saveWatchUpdateLogs(logText: String) {
        mPrefs.edit()?.putString(WATCH_UPDATE_LOGS, logText)?.commit()
    }

    override fun getLastMatchSelected(): String? {
        return mPrefs.getString(LAST_MATCH_SELECTED_EVENT, DateFormats.getTodaysDateString(7))
    }

    override fun setEndGameList(list: ArrayList<EndGame>) {
        mPrefs.edit()?.putString(END_GAME_LIST, gson.toJson(list))?.apply()
    }

    override fun getEndGameList(): ArrayList<EndGame> {
        return mPrefs.getString(END_GAME_LIST, null)
            ?.let { Gson().fromJson<ArrayList<EndGame>>(it) } ?: ArrayList()
    }

    override fun setEndGameValue(list: String) {
        mPrefs.edit()?.putString(END_GAME_VALUE, list)?.apply()
    }

    override fun getEndGameValue(): String {
        return mPrefs.getString(END_GAME_VALUE, null)?.let { (it) } ?: String()
    }


    override fun getFeatureIntervalFetchPeriod(): Int {
        return mPrefs.getInt(FEATURE_REFETCH_PERIOD, 24)
    }

    override fun getLogSyncInterval(): Int {
        return mPrefs.getInt(LOGS_SYNC_INTERVAL, 2)
    }

    override fun getLastStepsSyncWithServer(): Long {
        return mPrefs.getLong(STEPS_LAST_SYNC_WITH_SERVER, 0)
    }

    override fun setLastStepsSyncWithServer(timeStamp: Long) {
        mPrefs.edit()?.putLong(STEPS_LAST_SYNC_WITH_SERVER, timeStamp)?.apply()
    }

    override fun getLastTokenRefreshTimestamp(): Long {
        return mPrefs.getLong(TOKEN_LAST_UPDATE, 0)
    }

    override fun saveLastTokenRefreshTimestamp() {
        mPrefs.edit()?.putLong(TOKEN_LAST_UPDATE, System.currentTimeMillis())?.commit()
    }

    override fun getLastClearTables(): Long {
        return mPrefs.getLong(LAST_CLEAR_TABLE_TIMESTAMP, 0)
    }

    override fun setLastClearTables(timeStamp: Long) {
        mPrefs.edit()?.putLong(LAST_CLEAR_TABLE_TIMESTAMP, timeStamp)?.apply()
    }

    override fun getFirstOpenTimeStamp(): Long {
        return mPrefs.getLong(FIRST_OPEN_TIMESTAMP, 0)
    }

    override fun setFirstOpenTimeStamp(timeStamp: Long) {
        mPrefs.edit()?.putLong(FIRST_OPEN_TIMESTAMP, timeStamp)?.apply()
    }

    override fun getLastReviewShownTimeStamp(): Long {
        return mPrefs.getLong(REVIEW_SHOWN_TIMESTAMP, 0)
    }

    override fun setLastReviewShownTimeStamp(timeStamp: Long) {
        mPrefs.edit()?.putLong(REVIEW_SHOWN_TIMESTAMP, timeStamp)?.apply()
    }

    override fun getAwardCount(): Int {
        return mPrefs.getInt(AWARD_COUNT, 0)
    }

    override fun setAwardCount(count: Int) {
        mPrefs.edit()?.putInt(AWARD_COUNT, count)?.apply()
    }

    override fun getGoalCompletionCount(): Int {
        return mPrefs.getInt(GOAL_COMPLETION_COUNT, 0)
    }

    override fun setGoalCompletionCount(count: Int) {
        mPrefs.edit()?.putInt(GOAL_COMPLETION_COUNT, count)?.apply()
    }

    override fun getWorkoutShareCount(): Int {
        return mPrefs.getInt(WORKOUT_SHARE_COUNT, 0)
    }

    override fun setWorkoutShareCount(count: Int) {
        mPrefs.edit()?.putInt(WORKOUT_SHARE_COUNT, count)?.apply()
    }

    override fun getWatchFaceTransferCount(): Int {
        return mPrefs.getInt(WATCHFACE_TRANSFER_COUNT, 0)
    }

    override fun setWatchFaceTransferCount(count: Int) {
        mPrefs.edit()?.putInt(WATCHFACE_TRANSFER_COUNT, count)?.apply()
    }

    override fun getCustomWatchFaceTransferCount(): Int {
        return mPrefs.getInt(WATCHFACE_TRANSFER_CUSTOM_COUNT, 0)
    }


    override fun saveLastSportApiTimeStamp(timeStamp: Long) {
        mPrefs.edit()?.putLong(MATCH_INFO_TIMESTAMP, timeStamp)?.apply()
    }

    override fun getLastSportApiTimeStamp(): Long {
        return mPrefs.getLong(MATCH_INFO_TIMESTAMP, 0)
    }

    override fun isEnableIpl(): Boolean {
        return mPrefs.getBoolean(IPL_2022_STATE, false)
    }

    override fun setIplStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(IPL_2022_STATE, status)?.apply()
    }

    override fun getSportEvenApiCallInterval(): Int {
        return mPrefs.getInt(SPORT_EVENT_INTERVAL_TIME, 5)
    }

    override fun setSportEvenApiCallInterval(data: Int) {
        mPrefs.edit()?.putInt(SPORT_EVENT_INTERVAL_TIME, data)?.apply()
    }

    override fun getCacheSportEventList(): Matches? {
        return gson.fromJson(
            mPrefs.getString(SPORT_EVENT_CACHE_LIST, null), Matches::class.java
        )
    }

    override fun setCacheSportEventList(data: Matches) {
        mPrefs.edit()?.putString(SPORT_EVENT_CACHE_LIST, gson.toJson(data))?.apply()
    }


    override fun setCustomWatchFaceTransferCount(count: Int) {
        mPrefs.edit()?.putInt(WATCHFACE_TRANSFER_CUSTOM_COUNT, count)?.apply()
    }

    override fun saveFeatureIntervalFetchPeriod(resetInterval: Int) {
        mPrefs.edit()?.putInt(FEATURE_REFETCH_PERIOD, resetInterval)?.apply()
    }

    override fun saveLogSyncInterval(resetInterval: Int) {
        mPrefs.edit()?.putInt(LOGS_SYNC_INTERVAL, resetInterval)?.apply()
    }

    override fun getNotificationMessageClearStatus(): Boolean {
        return mPrefs.getBoolean(NOTIFICATION_SUMMARY_CLEAR_STATUS, false)
    }

    override fun setNotificationMessageClearedStatus(b: Boolean) {
        mPrefs.edit()?.putBoolean(NOTIFICATION_SUMMARY_CLEAR_STATUS, b)?.apply()
    }


    override fun getPrivacyPolicyStatus(): Boolean {
        return mPrefs.getBoolean(PRIVACY_POLICY_STATUS, false)
    }

    override fun setPrivacyPolicyStatus(b: Boolean) {
        mPrefs.edit()?.putBoolean(PRIVACY_POLICY_STATUS, b)?.apply()
    }

    override fun setBodyTempUnit(unit: Units) {
        mPrefs.edit()?.putString(BODY_TEMP_UNIT, unit.toString())?.apply()
    }

    override fun getBodyTempUnit(): Units {
        val enumString = mPrefs.getString(BODY_TEMP_UNIT, Units.IMPERIAL.name)
        return Units.getValueFromString(enumString)
    }

    override fun setBluetoothDialogShown(status: Boolean) {
        mPrefs.edit()?.putBoolean(BLUETOOTH_ENABLE_DIALOG, status)?.apply()
    }


    override fun isBluetoothDialogShown(): Boolean {
        return mPrefs.getBoolean(BLUETOOTH_ENABLE_DIALOG, false)
    }

    override fun clearNotificationGoalTimeStamp() {
        mPrefs.edit()?.putLong(NOTIFICATION_80_STATUS_TIME, 0)?.commit()

        mPrefs.edit()?.putLong(NOTIFICATION_GOAL_STATUS_TIME, 0)?.commit()
    }

    override fun get80NotificationTimeStamp(): Long {
        return mPrefs.getLong(NOTIFICATION_80_STATUS_TIME, 0)
    }

    override fun getDeviceFeaturesLastSyncTime(): Long {
        return mPrefs.getLong(DEVICE_FEATURE_SYNC_TIME, 0)
    }

    override fun setDeviceFeaturesLastSyncTime() {
        mPrefs.edit()?.putLong(DEVICE_FEATURE_SYNC_TIME, System.currentTimeMillis())?.apply()
    }

    override fun getNotificationCompleteTimeStamp(): Long {
        return mPrefs.getLong(NOTIFICATION_GOAL_STATUS_TIME, 0)
    }

    override fun get80NotificationStatus(): Boolean {
        return mPrefs.getBoolean(NOTIFICATION_80_STATUS, false)
    }

    override fun set80NotificationStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(NOTIFICATION_80_STATUS, status)?.commit()

        mPrefs.edit()?.putLong(NOTIFICATION_80_STATUS_TIME, System.currentTimeMillis())?.commit()
    }

    override fun setGoalCompleteNotificationStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(NOTIFICATION_GOAL_STATUS, status)?.commit()
        mPrefs.edit()?.putLong(NOTIFICATION_GOAL_STATUS_TIME, System.currentTimeMillis())?.commit()
    }

    override fun getGoalCompleteNotificationStatus(): Boolean {
        return mPrefs.getBoolean(NOTIFICATION_GOAL_STATUS, false)
    }

    override fun getAppOpenCount(): Int {
        return mPrefs.getInt(APP_OPEN_COUNT, 0)
    }

    override fun incrementAppOpenCount() {
        mPrefs.edit()?.putInt(APP_OPEN_COUNT, getAppOpenCount() + 1)?.commit()
    }

    override fun isAutoStartEnabled(): Boolean {
        return mPrefs.getBoolean(AUTO_START_FLAG, false)
    }

    override fun setAutoStart(b: Boolean) {
        mPrefs.edit()?.putBoolean(AUTO_START_FLAG, b)?.apply()
    }

    override fun saveCrashLog(crashlog: String) {
        mPrefs.edit()?.putString(CRASH_LOG, crashlog)?.apply()
    }

    override fun getCrashLog(): String? {
        return mPrefs.getString(CRASH_LOG, null)
    }

    override fun updateUserToken(token: Token?) {
        mPrefs.edit()?.putString(USER_TOKEN, gson.toJson(token))?.commit()
    }

    override fun getUserToken(): Token? {
        return gson.fromJson(mPrefs.getString(USER_TOKEN, null), Token::class.java)
    }

    override fun getTimeFormat(): String? {
        return mPrefs.getString(TIME_FORMAT, TimeFormats.HOURS_12.type)
    }

    override fun setTimeFormat(format: String) {
        mPrefs.edit()?.putString(TIME_FORMAT, format)?.commit()
    }

    override fun isEnableGoogleFit(): Boolean {
        return mPrefs.getBoolean(GOOGLE_FIT_STATUS, false)
    }

    override fun setGoogleFitStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(GOOGLE_FIT_STATUS, status)?.apply()
    }

    override fun setVerifyMobileNumberStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(VERIFY_MOBILE_NUMBER, status)?.apply()
    }

    override fun getVerifyMobileNumberStatus(): Boolean {
        return mPrefs.getBoolean(VERIFY_MOBILE_NUMBER, false)
    }

    override fun getChallengeInfo(): String? {
        return mPrefs.getString(CHALLENGE_INFO, null)
    }

    override fun saveLastWatchFace(watchFace: WatchFace) {
        mPrefs.edit()?.putString(LAST_WATCHFACE, gson.toJson(watchFace))?.apply()
    }

    override fun getLastWatchFace(): WatchFace? {
        return gson.fromJson(mPrefs.getString(LAST_WATCHFACE, null), WatchFace::class.java)
    }

    override fun clearLastWatchFace() {
        mPrefs.edit().remove(LAST_WATCHFACE).commit()

    }

    override fun getWarrantyStatus(): Int {
        return mPrefs.getInt(WARRANTY_STATUS, -1)
    }

    override fun getLastSyncTimeStamp(): Long {
        return mPrefs.getLong(LAST_SYNC, -1)
    }

    override fun saveLastSyncTimeStamp(timeStamp: Long) {
        mPrefs.edit()?.putLong(LAST_SYNC, timeStamp)?.commit()
    }

    override fun getLastWeatherSyncTimeStamp(): Long {
        return mPrefs.getLong(LAST_SYNC_WEATHER, 0)
    }

    override fun saveLastWeatherSyncTimeStamp(timeStamp: Long) {
        mPrefs.edit()?.putLong(LAST_SYNC_WEATHER, timeStamp)?.commit()
    }

    /**
     * -1 No Status
     * 0-> Not Registered
     * 1-> Registered
     */
    override fun setWarrantyStatus(status: Int) {
        mPrefs.edit()?.putInt(WARRANTY_STATUS, status)?.commit()
    }

    override fun setChallengeInfo(data: String) {
        mPrefs.edit()?.putString(CHALLENGE_INFO, data)?.apply()
    }

    override fun updateDeviceToken(token: String?) {
        mPrefs.edit()?.putString(DEVICE_TOKEN, token)?.apply()
    }

    override fun getDeviceToken(): String? {
        return mPrefs.getString(DEVICE_TOKEN, null)
    }

//    override fun updateUserProfile(userInfo: UserInfo?) {
//        mPrefs.edit()
//            ?.putString(USER_PROFILE, gson.toJson(userInfo))
//            ?.apply()
//    }
//
//    override fun getUserProfile(): UserInfo? {
//        return gson.fromJson(mPrefs.getString(USER_PROFILE, null), UserInfo::class.java)
//
//    }


    override fun setServiceState(state: ServiceState) {
        mPrefs.edit()?.putString(CONNECTION_SERVICE_STATE, state.name)?.apply()
    }


    override fun getServiceState(): ServiceState {
        return ServiceState.valueOf(
            mPrefs.getString(
                CONNECTION_SERVICE_STATE, ServiceState.STOPPED.name
            )!!
        )
    }


    override fun getUnit(): Units {
        return getUser()?.userGoals?.getUnit() ?: Units.METRIC

    }

    override fun saveTemperatureUnit(unit: Units) {
        mPrefs.edit()?.putString(TEMPERATURE_UNIT, unit.toString())?.apply()
    }

    override fun getTemperatureUnit(): Units {
        val enumString = mPrefs.getString(TEMPERATURE_UNIT, "")
        return Units.getValueFromString(enumString)
    }

    override fun saveDeviceFeatures(deviceFeatures: DeviceFeatures) {
        mPrefs.edit()?.putString(DEVICE_FEATURES, gson.toJson(deviceFeatures))?.commit()
        setDeviceFeaturesLastSyncTime()
    }

    override fun getDeviceFeatures(): DeviceFeatures? {
        return gson.fromJson(mPrefs.getString(DEVICE_FEATURES, null), DeviceFeatures::class.java)
    }


    override fun removeNotificationApp(app: NotificationApp) {
        val appList = getNotificationEnabledAppList()
        if (appList.isNullOrEmpty()) return

        mPrefs.edit()?.putString(NEW_ENABLED_NOTIFICATION_APP, gson.toJson(appList))?.commit()
    }

    private fun removeApp(app: NotificationApp, appList: ArrayList<NotificationApp>): Boolean {
        appList.forEach {
            if (it.appCode == app.appCode) {
                appList.remove(it)
                return true
            }
        }
        return false

    }

    override fun clearNotificationAppList() {
        mPrefs.edit().remove(NEW_ENABLED_NOTIFICATION_APP).commit()
    }

    override fun clearOldNotificationAppList() {
        mPrefs.edit().remove(ENABLED_NOTIFICATION_APP).commit()
    }

    override fun getNotificationEnabledAppList(): List<NotificationApp>? {
        val type = object : TypeToken<List<NotificationApp?>?>() {}.type
        return gson.fromJson<List<NotificationApp>>(
            mPrefs.getString(
                NEW_ENABLED_NOTIFICATION_APP, null
            ), type
        )
    }

    override fun getOldNotificationEnabledAppList(): List<NotificationApp>? {
        val type = object : TypeToken<List<NotificationApp?>?>() {}.type
        return gson.fromJson<List<NotificationApp>>(
            mPrefs.getString(
                ENABLED_NOTIFICATION_APP, null
            ), type
        )
    }

    override fun isNotificationAlertEnabled(): Boolean {
        return mPrefs.getBoolean(NOTIFICATION_ALERT_STATUS, false)
    }

    override fun setNotificationAlertStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(NOTIFICATION_ALERT_STATUS, status)?.commit()
    }

    override fun saveNotificationAppList(app: List<NotificationApp>) {
        app.forEach {
            it.imageDrawable = null
        }
        mPrefs.edit()?.putString(NEW_ENABLED_NOTIFICATION_APP, gson.toJson(app))?.commit()
    }

    override fun updateLocations(location: Location) {
        mPrefs.edit()?.putString(LOCATIONS, gson.toJson(location))?.commit()
    }

    override fun getLocation(): Location? {
        return gson.fromJson(mPrefs.getString(LOCATIONS, null), Location::class.java)

    }

    override fun saveSchedulerTimeAndFrequency(interval: Int, syncFrequency: Int) {
        mPrefs.edit()?.putInt(SCHEDULER_TIME_INTERVAL, interval)
            ?.putInt(SYNC_INTERVAL_FREQUENCY, syncFrequency)?.apply()
    }

    override fun getSchedulerTimeInterval(): Int {
        return mPrefs.getInt(SCHEDULER_TIME_INTERVAL, 0)

    }

    override fun getSyncIntervalFrequency(): Int {
        return mPrefs.getInt(SYNC_INTERVAL_FREQUENCY, 0)
    }

    override fun saveSyncTriedNumber(num: Int) {
        mPrefs.edit()?.putInt(SYNC_TRIED_NUMBER, num)?.apply()
    }

    override fun getSyncTriedNumber(): Int {
        return mPrefs.getInt(SYNC_TRIED_NUMBER, 0)
    }

    override fun clearConnectedDevice() {
        mPrefs.edit().remove(DEVICE_SETUP_STATUS_NEW).apply()
        /*
         WatchInfoGlobals.hideBleCallingDialogForThisSession = false
         mPrefs.edit().remove(WF_RATING_KEY).commit()
         mPrefs.edit().remove(SLEEP_LOCAL_NOTIFICATION_KEY).commit()
         mPrefs.edit().remove(USER_LOCATION_MAPPED_KEY).commit()
         mPrefs.edit().remove(DEVICE_TOKEN).commit()
         mPrefs.edit().remove(LAST_SYNC).commit()
         mPrefs.edit().remove(ADDED_RECOMMENDED_NOTIFICATION).commit()
         mPrefs.edit().remove(WATCH_FACE_CATEGORY_RESPONSE).commit()
         mPrefs.edit().remove(LAST_WATCHFACE).commit()
         mPrefs.edit().remove(LAST_SYNC_WEATHER).commit()
         mPrefs.edit().remove(DEVICE_FEATURE_SYNC_TIME).commit()
         mPrefs.edit().remove(STEPS_LAST_SYNC_WITH_SERVER).commit()
         mPrefs.edit().remove(DEVICE_FEATURES).commit()
         mPrefs.edit().remove(EDIT_DASHBOARD_LIST).commit()
         mPrefs.edit().remove(PROMITONAL_BANNER_STATE).commit()
         mPrefs.edit().remove(WATCH_UPDATE_LOGS).apply()
         mPrefs.edit().remove(BLUETOOTH_ENABLE_DIALOG).apply()
         mPrefs.edit().remove(LAST_INFO_FETCH_TIME).apply()
         mPrefs.edit().remove(BATTERY_NOTIFICATION_KEY).apply()
         mPrefs.edit().remove(CHARGE_O_NIGHT_NOTIFICATION_KEY).apply()
         mPrefs.edit().remove(BATTERY_NOTIFICATION_LAST_STATE_KEY).apply()
         mPrefs.edit().remove(AGPS_STATE_KEY).apply()
         clearLastWatchFace()
         clearRandomWatchFaceListResponse()
         clearNotificationAppList()
         setNotificationAlertStatus(false)
         setWarrantyStatus(-1)*/
    }

    override fun getLastPeriodicDataSyncTime(): Long {
        return mPrefs.getLong(PERIODIC_DATA_TIMESTAMP, DateFormats.getTimeStamp())
    }

    override fun setLastPeriodicDataSyncTime(timeStamp: Long) {
        mPrefs.edit()?.putLong(PERIODIC_DATA_TIMESTAMP, timeStamp)?.apply()
    }

    override fun updateNotificationSettings(notificationSettings: AppNotificationsSettings?) {
        mPrefs.edit()?.putString(NOTIFICATION_SETTINGS, gson.toJson(notificationSettings))?.apply()
    }

    override fun getNotificationSettings(): AppNotificationsSettings? {
        return gson.fromJson(
            mPrefs.getString(NOTIFICATION_SETTINGS, null), AppNotificationsSettings::class.java
        )
    }

    override fun updateEnabledAppsForNotification(enabledAppsForNotifications: EnabledAppsForNotifications?) {
        mPrefs.edit()
            ?.putString(ENABLED_APPS_FOR_NOTIFICATION, gson.toJson(enabledAppsForNotifications))
            ?.apply()
    }

    override fun getEnabledAppsForNotification(): EnabledAppsForNotifications? {
        return gson.fromJson(
            mPrefs.getString(ENABLED_APPS_FOR_NOTIFICATION, null),
            EnabledAppsForNotifications::class.java
        )

    }

    override fun updateWeatherSettings(status: Boolean) {
        mPrefs.edit()?.putBoolean(WEATHER_SWITCH, status)?.apply()
    }

    override fun getWeatherSwitch(): Boolean {
        return mPrefs.getBoolean(WEATHER_SWITCH, false)
    }


    override fun getPreferencesWatchLogsName(default_value: String?): String? {
        return mPrefs.getString(WATCH_LOGS_NAME, null)

    }

    override fun saveUserInfo(user: User) {
        mPrefs.edit()?.putString(USER_INFO, gson.toJson(user))?.commit()
    }

    override fun deleteUserInfo() {
        mPrefs.edit().remove(USER_INFO).commit()
        mPrefs.edit().remove(LOCAL_USER_DATA).commit()
    }

    /**
     * Remove Device Token and user token
     */
    override fun deleteUserToken() {
        mPrefs.edit().remove(USER_TOKEN).commit()
        mPrefs.edit().remove(DEVICE_TOKEN).commit()
    }

    override fun deleteFcmToken() {
        mPrefs.edit().remove(FCM_LAST_TOKEN).commit()
    }

    override fun setFcmToken(token: String) {
        mPrefs.edit()?.putString(FCM_LAST_TOKEN, token)?.commit()
    }

    override fun getFcmToken(): String? {
        return mPrefs.getString(FCM_LAST_TOKEN, null)
    }

    override fun deleteAdditionalInfo() {
        mPrefs.edit().remove(USER_PROFILE).apply()
        mPrefs.edit().remove(USER_GOALS).apply()
    }

    override fun getUserDataSyncStatus(): Boolean {
        return mPrefs.getBoolean(USER_INFO_SYNCED, false)
    }

    override fun setUserDataSynced(status: Boolean) {
        mPrefs.edit()?.putBoolean(USER_INFO_SYNCED, status)?.commit()
    }

    override fun getUser(): User? {
        val userJson = mPrefs.getString(USER_INFO, null)
        if (userJson.isNullOrEmpty()) return null
        return gson.fromJson(userJson, User::class.java)
    }

    override fun updateUserActivities(healthOverviewData: HealthOverviewData) {
        mPrefs.edit()?.putString(USER_ACTIVITIES, gson.toJson(healthOverviewData))?.apply()
    }

    override fun getUserActivities(): HealthOverviewData? {
        val activityJson = mPrefs.getString(USER_ACTIVITIES, null)
        if (activityJson.isNullOrEmpty()) return null
        return gson.fromJson(activityJson, HealthOverviewData::class.java)

    }

    override fun isCallAlertEnabled(): Boolean {
        return mPrefs.getBoolean(CALL_ALERT, false)
    }

    override fun setCallAlertStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(CALL_ALERT, status)?.apply()
    }

    override fun isSMSAlertEnabled(): Boolean {
        return mPrefs.getBoolean(SMS_ALERT, false)
    }

    override fun setSMSAlertStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(SMS_ALERT, status)?.apply()
    }

    override fun setIgnoreVersion(number: Int) {
        mPrefs.edit()?.putInt(IGNORE_VERSION_NUMBER, number)?.apply()
    }

    override fun getIgnoreVersion(): Int {
        return mPrefs.getInt(IGNORE_VERSION_NUMBER, 0)
    }

    override fun setLastSyncWithServer(timeStamp: Long) {
        mPrefs.edit()?.putLong(LAST_SYNC_WITH_SERVER, timeStamp)?.apply()
    }

    override fun getLastSyncWithServer(): Long {
        return mPrefs.getLong(LAST_SYNC_WITH_SERVER, 0)
    }

    override fun setStepsLastSyncHash(hash: String) {
        mPrefs.edit()?.putString(STEPS_LAST_SYNC_HASH, hash)?.apply()
    }

    override fun getStepsLastSyncHash(): String? {
        return mPrefs.getString(STEPS_LAST_SYNC_HASH, "")
    }

    override fun setHeartLastSyncHash(hash: String) {
        mPrefs.edit()?.putString(HEART_LAST_SYNC_HASH, hash)?.apply()
    }

    override fun getHeartLastSyncHash(): String? {
        return mPrefs.getString(HEART_LAST_SYNC_HASH, "")
    }

    override fun setBodyTempSyncHash(hash: String) {
        mPrefs.edit()?.putString(BODY_TEMP_LAST_SYNC_HASH, hash)?.apply()
    }

    override fun getBodyTempSyncHash(): String? {
        return mPrefs.getString(BODY_TEMP_LAST_SYNC_HASH, "")
    }

    override fun setStressLastSyncHash(hash: String) {
        mPrefs.edit()?.putString(STRESS_LAST_SYNC_HASH, hash)?.apply()
    }

    override fun getStressLastSyncHash(): String? {
        return mPrefs.getString(STRESS_LAST_SYNC_HASH, "")
    }

    override fun setBloodOxygenLastSyncHash(hash: String) {
        mPrefs.edit()?.putString(BLOOD_LAST_SYNC_HASH, hash)?.apply()
    }

    override fun getBloodOxygenLastSyncHash(): String? {
        return mPrefs.getString(BLOOD_LAST_SYNC_HASH, "")
    }

    override fun setSleepLastSyncHash(hash: String) {
        mPrefs.edit()?.putString(SLEEP_LAST_SYNC_HASH, hash)?.apply()
    }

    override fun getSleepLastSyncHash(): String? {
        return mPrefs.getString(SLEEP_LAST_SYNC_HASH, "")
    }

    override fun saveAndGetLocation(data: List<LocationDataModel>): List<LocationDataModel> {
        val finalLocationList = java.util.ArrayList<LocationDataModel>()
        val gson = Gson()
        val hasRun = mPrefs.getString(MAPS_LAT_LONG, null)
        val edit = mPrefs.edit()
        if (hasRun == null) {
            finalLocationList.addAll(data)
            edit.putString(MAPS_LAT_LONG, gson.toJson(data))
        } else {
            val type = object : TypeToken<java.util.ArrayList<LocationDataModel?>?>() {}.type
            val arrayList = gson.fromJson<java.util.ArrayList<LocationDataModel>>(hasRun, type)
            arrayList.addAll(data)
            edit.putString(MAPS_LAT_LONG, gson.toJson(arrayList))
            finalLocationList.addAll(arrayList)
        }
        edit.commit()
        return finalLocationList
    }

    override fun clearLocation() {
        mPrefs.edit().remove(MAPS_LAT_LONG).commit()
    }

    override fun getLastInfoFetchTime(): Long {
        return mPrefs.getLong(LAST_INFO_FETCH_TIME, 0)
    }

    override fun setLastInfoFetchTime(timeStamp: Long) {
        mPrefs.edit()?.putLong(LAST_INFO_FETCH_TIME, timeStamp)?.apply()
    }

    override fun showEnableBgPermissionDialog(status: Boolean) {
        mPrefs.edit()?.putBoolean(ENABLE_BG_PERMISSION, status)?.apply()
    }

    override fun getEnableBgPermissionDialog(): Boolean {
        return mPrefs.getBoolean(ENABLE_BG_PERMISSION, false)
    }

    override fun getLastBannerApiCallFetchTime(): Long {
        return mPrefs.getLong(LAST_BANNER_API_FETCH_TIME, 0)
    }

    override fun setLastBannerApiCallFetchTime(timeStamp: Long) {
        mPrefs.edit()?.putLong(LAST_BANNER_API_FETCH_TIME, timeStamp)?.apply()
    }

    override fun setBannerList(list: ArrayList<DashboardBanner>) {
        mPrefs.edit()?.putString(BANNER_LIST, gson.toJson(list))?.apply()
    }

    override fun getBannerList(): ArrayList<DashboardBanner> {
        return mPrefs.getString(BANNER_LIST, null)
            ?.let { Gson().fromJson<ArrayList<DashboardBanner>>(it) } ?: ArrayList()
    }


    override fun setNoiseFitContactCount(count: Int) {
        mPrefs.edit()?.putInt(NOISE_FIT_CONTACT_COUNT, count)?.commit()
    }

    override fun isHoldPressPopEmojiViewShown(): Boolean {
        return mPrefs.getBoolean(DEMO_EMOJI_POPUP_KEY, false)
    }

    override fun setHoldPressPopEmojiViewShown(data: Boolean) {
        mPrefs.edit()?.putBoolean(DEMO_EMOJI_POPUP_KEY, data)?.commit()
    }


    override fun getNoiseFitContactCount(): Int {
        return mPrefs.getInt(NOISE_FIT_CONTACT_COUNT, -1)
    }

    override fun setYearlyEndGoal(goal: String) {
        mPrefs.edit()?.putString(YEAR_END_GOAL, goal)?.apply()
    }

    override fun getYearlyEndGoal(): String? {
        return mPrefs.getString(YEAR_END_GOAL, "")
    }

    override fun deleteYearlyGoal() {
        mPrefs.edit().remove(YEAR_END_GOAL).apply()
    }

    override fun getAGPSStatusState(): com.noisefit_commans.data.model.AGPSStatusState? {
        return mPrefs.getString(AGPS_STATE_KEY, null)
            ?.let { Gson().fromJson<com.noisefit_commans.data.model.AGPSStatusState>(it) }
    }

    override fun setAGPSStatusState(data: com.noisefit_commans.data.model.AGPSStatusState) {
        mPrefs.edit()?.putString(AGPS_STATE_KEY, gson.toJson(data))?.apply()
    }

    override fun setActivitySyncCount(count: Int) {
        mPrefs.edit()
            ?.putInt(ACTIVITY_SYNC_COUNT, count)
            ?.commit()
    }

    override fun getActivitySyncCount(): Int {
        return mPrefs.getInt(ACTIVITY_SYNC_COUNT, -1)
    }

    override fun setJoinedChallengeCount(count: Int) {
        mPrefs.edit()
            ?.putInt(JOINED_CHALLENGE_COUNT, count)
            ?.commit()
    }

    override fun getJoinedChallengeCount(): Int {
        return mPrefs.getInt(JOINED_CHALLENGE_COUNT, -1)
    }

    override fun setFriendCount(count: Int) {
        mPrefs.edit()
            ?.putInt(FRIEND_COUNT, count)
            ?.commit()
    }

    override fun getFriendCount(): Int {
        return mPrefs.getInt(FRIEND_COUNT, -1)
    }

    override fun setQrCodeInfoStatus(b: Boolean) {
        mPrefs.edit()
            ?.putBoolean(QR_INFO_STATUS, b)
            ?.apply()
    }

    override fun getQrCodeInfoStatus(): Boolean {
        return mPrefs.getBoolean(QR_INFO_STATUS, false)
    }

    override fun setIsWatchFaceRewardEarned(b: Boolean) {
        mPrefs.edit()
            ?.putBoolean(WF_REWARDS, b)
            ?.apply()
    }

    override fun getIsWatchFaceRewardEarned(): Boolean {
        return mPrefs.getBoolean(WF_REWARDS, false)
    }

    override fun setSleepLastLocalNotification(date: String) {
        mPrefs.edit()
            ?.putString(SLEEP_LOCAL_NOTIFICATION_KEY, date)
            ?.apply()
    }

    override fun getSleepLastLocalNotification(): String? {
        return mPrefs.getString(SLEEP_LOCAL_NOTIFICATION_KEY, null)
    }

    override fun getTimeLineCurrentPageCount(): Int {
        return mPrefs.getInt(TIMELINE_PAGE_COUNT, 1)
    }

    override fun setTimeLineCurrentPageCount(page: Int) {
        mPrefs.edit()
            ?.putInt(TIMELINE_PAGE_COUNT, page)
            ?.commit()
    }


    override fun setNplWalkAround(status: Boolean) {
        mPrefs.edit()?.putBoolean(NPL_WALKAROUND_KEY, status)?.apply()
    }

    override fun isNplWalkAroundShown(): Boolean {
        return mPrefs.getBoolean(NPL_WALKAROUND_KEY, false)
    }

    override fun getNplPrivacyPolicyStatus(): Boolean {
        return mPrefs.getBoolean(NPL_PRIVACY_POLICY_STATUS, false)
    }

    override fun setNplPrivacyPolicyStatus(b: Boolean) {
        mPrefs.edit()?.putBoolean(NPL_PRIVACY_POLICY_STATUS, b)?.apply()
    }

    override fun getLastWinsCount(): Int {
        return mPrefs.getInt(NPL_WINS_COUNT, -1)
    }

    override fun setLastWinsCount(winCount: Int) {
        mPrefs.edit()?.putInt(NPL_WINS_COUNT, winCount)?.apply()
    }

    override fun getQuizApiCallTimeStamp(): Long {
        return mPrefs.getLong(QUIZ_API_CALL_TIME, -1)
    }

    override fun setWatchFaceRatedId(id: Int) {
        val gson = Gson()
        val hasRun = mPrefs.getString(WF_RATING_KEY, null)
        val edit = mPrefs.edit()
        if (hasRun == null) {
            val list = ArrayList<Int>()
            list.add(id)
            edit.putString(WF_RATING_KEY, gson.toJson(list))
        } else {
            val type = object : TypeToken<ArrayList<Int>>() {}.type
            val arrayList = gson.fromJson<ArrayList<Int>>(hasRun, type)
            arrayList.add(id)
            edit.putString(WF_RATING_KEY, gson.toJson(arrayList))

        }
        edit.commit()

    }

    override fun clearWatchFaceRatedId(id: Int) {
        val hasRun = mPrefs.getString(WF_RATING_KEY, null)
        val edit = mPrefs.edit()
        if (hasRun != null) {
            val type = object : TypeToken<ArrayList<Int>>() {}.type
            val ratingList = gson.fromJson<ArrayList<Int>>(hasRun, type)

            val index = ratingList.indexOfFirst { it == id }

            if (index != -1) {
                ratingList.removeAt(index)
                edit.putString(WF_RATING_KEY, gson.toJson(ratingList))
                edit.commit()
            }
        }
    }

    override fun checkWatchFaceRatedIdExist(id: Int): Boolean {
        val hasRun = mPrefs.getString(WF_RATING_KEY, null)

        if (hasRun != null) {
            val type = object : TypeToken<ArrayList<Int>>() {}.type
            val ratingList = gson.fromJson<ArrayList<Int>>(hasRun, type)

            val index = ratingList.indexOfFirst { it == id }

            if (index != -1) {
                return true
            }
        }
        return false
    }


    override fun setQuizApiCallTimeStamp(timeStamp: Long) {
        mPrefs.edit()?.putLong(QUIZ_API_CALL_TIME, timeStamp)?.apply()
    }

    override fun getQuizQuestion(): NplQuizDataModel? {
        val quizData = mPrefs.getString(QUIZ_QUESTION_DATA, null)
        if (quizData.isNullOrEmpty()) return null
        return gson.fromJson(quizData, NplQuizDataModel::class.java)
    }

    override fun setQuizQuestion(quizData: NplQuizDataModel) {
        mPrefs.edit()?.putString(QUIZ_QUESTION_DATA, gson.toJson(quizData))?.apply()
    }

    override fun clearQuizQuestionData() {
        mPrefs.edit().remove(QUIZ_QUESTION_DATA).apply()
    }


    override fun setFeedPostCreateCount(count: Int) {
        mPrefs.edit()?.putInt(FEEDS_POST_COUNT, count)?.apply()
    }

    override fun getFeedPostCreateCount(): Int {
        return mPrefs.getInt(FEEDS_POST_COUNT, 0)
    }

    override fun setTenDayLaterNowLasTimeStamp(timeStamp: Long) {
        mPrefs.edit()?.putLong(TEN_DAY_REVIEW_SHOWN_TIMESTAMP, timeStamp)?.apply()
    }

    override fun getTenDayLaterNowLastTimeStamp(): Long {
        return mPrefs.getLong(TEN_DAY_REVIEW_SHOWN_TIMESTAMP, 0)
    }

    override fun setLaterNowLastReviewShownTimeStamp(timeStamp: Long) {
        mPrefs.edit()?.putLong(LATER_NOW_REVIEW_SHOWN_TIMESTAMP, timeStamp)?.apply()
    }

    override fun getLaterNowLastReviewShownTimeStamp(): Long {
        return mPrefs.getLong(LATER_NOW_REVIEW_SHOWN_TIMESTAMP, 0)
    }

    override fun setShowReviewPopUp(boolean: Boolean) {
        mPrefs.edit()?.putBoolean(SHOW_REVIEW_POP_UP, boolean)?.apply()
    }

    override fun isShowReviewPopUp(): Boolean {
        return mPrefs.getBoolean(SHOW_REVIEW_POP_UP, false)
    }


}