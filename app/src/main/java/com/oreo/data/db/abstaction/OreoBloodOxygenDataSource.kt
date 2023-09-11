package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.models.BloodOxygenBreakup

interface OreoBloodOxygenDataSource {
    suspend fun insertData(
        data: OreoBloodOxygenBreakup
    ): Boolean

    suspend fun getTodayData(date: String): OreoBloodOxygenBreakup?
    suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoBloodOxygenBreakup>?
    suspend fun deleteOldData(timeStamp: Long): Int
    suspend fun updateServerSyncData(dataList: List<OreoBloodOxygenBreakup>, timeStamp: Long): Int
    suspend fun checkHalfSyncData()
}