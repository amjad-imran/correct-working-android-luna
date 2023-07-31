package com.noisefit.data.local

import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit_commans.data.model.KeyValue

class FakeKeyValueDataSourceImpl
constructor() : KeyValueDataSource {
    override suspend fun insertData(keyValue: KeyValue) {
        TODO("Not yet implemented")
    }

    override suspend fun getData(key: String, type: KeyValueDataType): KeyValue? {
        TODO("Not yet implemented")
    }

    override suspend fun removeDataByKey(key: String, type: KeyValueDataType) {
        TODO("Not yet implemented")
    }

    override suspend fun removeDataByType(type: KeyValueDataType) {
        TODO("Not yet implemented")
    }
}