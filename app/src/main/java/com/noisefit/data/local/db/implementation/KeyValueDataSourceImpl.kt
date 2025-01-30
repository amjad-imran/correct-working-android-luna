package com.noisefit.data.local.db.implementation

import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.local.db.database.KeyValueDao
import com.noisefit_commans.data.model.KeyValue
import javax.inject.Inject

class KeyValueDataSourceImpl
@Inject
constructor(
    private val keyValueDao: KeyValueDao
) : KeyValueDataSource {

    override suspend fun insertData(keyValue: KeyValue) {
        keyValueDao.insert(keyValue)
    }

    override suspend fun updateData(keyValue: KeyValue) {
        keyValueDao.update(keyValue)
    }
    override suspend fun getData(key: String, type: KeyValueDataType): KeyValue? {
        return keyValueDao.getData(key, type.name)
    }

    override suspend fun removeDataByKey(key: String, type: KeyValueDataType) {
        keyValueDao.deleteByKey(key, type.name)
    }

    override suspend fun removeDataByType(type: KeyValueDataType) {
        keyValueDao.deleteByType(type.name)
    }
}