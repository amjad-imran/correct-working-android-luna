package com.noisefit.data.repository

import com.noisefit.data.local.dataStored.abstraction.ILastSyncStore
import com.noisefit_commans.utils.DateFormats
import javax.inject.Inject

class LastSyncProvider
@Inject
constructor(
    private val lastSyncStore: ILastSyncStore
) {

    private val PREFIX = "LAST_SYNC_"

    fun getSyncTimeStamp(key: LastSyncItems): Long {
        return lastSyncStore.getLongValue("$PREFIX${key.name}")
    }

    fun setSyncTimeStamp(key: LastSyncItems) {
        lastSyncStore.saveLongValue("$PREFIX${key.name}", DateFormats.getTimeStamp())
    }

    fun setSyncTimeStamp(key: LastSyncItems, timeStamp: Long) {
        lastSyncStore.saveLongValue("$PREFIX${key.name}", timeStamp)
    }

    fun removeSyncTimeStamp(key: LastSyncItems) {
        lastSyncStore.saveLongValue("$PREFIX${key.name}", 0)
    }

    fun removeSyncTimeStamp(list: List<LastSyncItems>) {
        list.forEach {
            removeSyncTimeStamp(it)
        }
    }


    fun removeUserDataLastSync() {
        removeSyncTimeStamp(
            listOf(
                LastSyncItems.HEALTH_STEPS_DAY,
                LastSyncItems.HEALTH_STEPS_WEEK,
                LastSyncItems.HEALTH_STEPS_MONTH,
                LastSyncItems.HEALTH_STEPS_YEAR,
                LastSyncItems.HEALTH_STEPS_HIGHLIGHT,
                LastSyncItems.HEALTH_STRESS_DAY,
                LastSyncItems.HEALTH_STRESS_WEEK,
                LastSyncItems.HEALTH_STRESS_MONTH,
                LastSyncItems.HEALTH_STRESS_YEAR,
                LastSyncItems.RECENT_ACTIVITIES,
                LastSyncItems.HEALTH_BO_DAY,
                LastSyncItems.HEALTH_BO_WEEK,
                LastSyncItems.HEALTH_BO_MONTH,
                LastSyncItems.HEALTH_BO_YEAR,
                LastSyncItems.HEALTH_HR_DAY,
                LastSyncItems.HEALTH_HR_WEEK,
                LastSyncItems.HEALTH_HR_MONTH,
                LastSyncItems.HEALTH_HR_YEAR,
                LastSyncItems.HEALTH_BODY_TEMP_DAY,
                LastSyncItems.HEALTH_BODY_TEMP_WEEK,
                LastSyncItems.HEALTH_BODY_TEMP_MONTH,
                LastSyncItems.HEALTH_BODY_TEMP_YEAR,
                LastSyncItems.HEALTH_SLEEP_DAY,
                LastSyncItems.HEALTH_SLEEP_WEEK,
                LastSyncItems.HEALTH_SLEEP_MONTH,
                LastSyncItems.HEALTH_SLEEP_YEAR,
                LastSyncItems.HEALTH_SLEEP_HIGHLIGHT,
                LastSyncItems.CHALLENGE_LIST,
                LastSyncItems.CHALLENGE_LIST_CURRENT,
                LastSyncItems.CHALLENGE_LIST_COMPLETED,
                LastSyncItems.WATCHFACE_FEEDBACK,
                LastSyncItems.SUMMARY_SERVER_TIMESTAMP,
                LastSyncItems.SUMMARY_LOCAL_TIMESTAMP
            )
        )
    }
}

enum class LastSyncItems {
    DASHBOARD_BANNER_1, DASHBOARD_BANNER_SERVER_UPDATE_1, SUMMARY_LOCAL_TIMESTAMP, SUMMARY_SERVER_TIMESTAMP,
    WORKOUT_IMAGES_LOCAL_TIMESTAMP, WORKOUT_IMAGES_SERVER_TIMESTAMP,
    FAVOURITES_WATCHFACE, WATCHFACE_MAIN_LIST, WATCHFACE_CATEGORIES, RECENT_ACTIVITIES,
    HEALTH_STEPS_DAY, HEALTH_STEPS_WEEK, HEALTH_STEPS_MONTH, HEALTH_STEPS_YEAR, HEALTH_STEPS_HIGHLIGHT,
    HEALTH_STRESS_DAY, HEALTH_STRESS_WEEK, HEALTH_STRESS_MONTH, HEALTH_STRESS_YEAR,
    HEALTH_BO_DAY, HEALTH_BO_WEEK, HEALTH_BO_MONTH, HEALTH_BO_YEAR,
    HEALTH_HR_DAY, HEALTH_HR_WEEK, HEALTH_HR_MONTH, HEALTH_HR_YEAR,
    HEALTH_BODY_TEMP_DAY, HEALTH_BODY_TEMP_WEEK, HEALTH_BODY_TEMP_MONTH, HEALTH_BODY_TEMP_YEAR,
    HEALTH_SLEEP_DAY, HEALTH_SLEEP_WEEK, HEALTH_SLEEP_MONTH, HEALTH_SLEEP_YEAR, HEALTH_SLEEP_HIGHLIGHT,
    CHALLENGE_LIST,
    CHALLENGE_LIST_CURRENT, CHALLENGE_LIST_COMPLETED,
    WATCHFACE_FEEDBACK, PAIRING_FEEDBACK,
    HELP_AND_SUPPORT_LIST, H_AND_SUPPORT_SERVER_UPDATE,WATCHFACE_2_SUB_CATEGORIES
}