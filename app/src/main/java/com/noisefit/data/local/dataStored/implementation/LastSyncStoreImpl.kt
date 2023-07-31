package com.noisefit.data.local.dataStored.implementation


import android.content.SharedPreferences
import com.noisefit.data.local.dataStored.abstraction.ILastSyncStore
import javax.inject.Inject


class LastSyncStoreImpl
@Inject
constructor(
    private val mPrefs: SharedPreferences
) : ILastSyncStore {
    override fun getLongValue(key: String): Long {
        return mPrefs.getLong(key, 0)
    }

    override fun saveLongValue(key: String, value: Long) {
        mPrefs.edit()
            ?.putLong(key, value)
            ?.apply()
    }
}