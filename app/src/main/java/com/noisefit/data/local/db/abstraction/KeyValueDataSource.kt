package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.data.model.KeyValue

const val CACHE_CLEAR_DEFAULT = 5

interface KeyValueDataSource {

    suspend fun insertData(
        keyValue: KeyValue
    )

    suspend fun getData(key: String, type: KeyValueDataType): KeyValue?

    suspend fun removeDataByKey(key: String, type: KeyValueDataType)

    suspend fun removeDataByType(type: KeyValueDataType)
}

enum class KeyValueDataType {
    DASHBOARD
}