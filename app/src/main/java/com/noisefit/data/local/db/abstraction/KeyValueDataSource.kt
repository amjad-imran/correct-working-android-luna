package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.data.model.KeyValue

const val CACHE_CLEAR_DEFAULT = 5

interface KeyValueDataSource {

    suspend fun insertData(
        keyValue: KeyValue
    )

    suspend fun updateData(
        keyValue: KeyValue
    )

    suspend fun getData(key: String, type: KeyValueDataType): KeyValue?

    suspend fun removeDataByKey(key: String, type: KeyValueDataType)

    suspend fun removeDataByType(type: KeyValueDataType)
}

enum class KeyValueDataType {
    CONTRIBUTORS, H_AND_S, LEARN, RECORD_WORKOUT, FEMALE_CYCLE_HISTORY, FEMALE_SYMPTOMS_ICON, FEMALE_HEALTH_CURRENT_DAY_V2,
    SLEEP_PLANNER, NOTIFICATION_GOAL_TOGGLE, NOTIFICATION_GOAL_DATA, CIRCADIAN_DATA, INSIGHTS_LIFE_OS_DATA
}