package com.noisefit.data.local

import com.noisefit.data.local.dataStored.abstraction.ILastSyncStore

class FakeLastSyncStoreSourceImpl
constructor() : ILastSyncStore {
    override fun getLongValue(key: String): Long {
        TODO("Not yet implemented")
    }

    override fun saveLongValue(key: String, value: Long) {
        TODO("Not yet implemented")
    }
}