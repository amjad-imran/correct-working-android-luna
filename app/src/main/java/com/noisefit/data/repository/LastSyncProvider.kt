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

}

enum class LastSyncItems {
    H_AND_SUPPORT_SERVER_UPDATE, HELP_AND_SUPPORT_LIST, PAIRING_FEEDBACK
}