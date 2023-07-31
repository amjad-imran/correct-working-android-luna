package com.noisefit.data.local.dataStored.abstraction


interface ILastSyncStore {
    fun getLongValue(key: String): Long
    fun saveLongValue(key: String, value: Long)
}