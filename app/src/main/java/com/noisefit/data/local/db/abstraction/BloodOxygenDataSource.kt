package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.models.BloodOxygenBreakup

interface BloodOxygenDataSource {
    suspend fun insertData(
        data: List<BloodOxygenBreakup>
    ): Boolean

    suspend fun getTodayData(date: String): List<BloodOxygenBreakup>?
    suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<BloodOxygenBreakup>?
    suspend fun deleteOldData(timeStamp: Long): Int
    suspend fun updateServerSyncData(dataList: List<BloodOxygenBreakup>, timeStamp: Long): Int
    suspend fun checkHalfSyncData()
}