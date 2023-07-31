package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.models.SleepData

interface SleepDataSource {

    suspend fun insertData(
        data: SleepData
    ): Boolean

    suspend fun getTodayData(date: String): List<SleepData>?
    suspend fun getServerUnSyncData(): List<SleepData>?

    suspend fun getUnSyncServerData(endDate:Long,isSync:Boolean): List<SleepData>?
    suspend fun deleteOldData(timeStamp: Long): Int
    suspend fun updateServerSyncData(dataList: List<SleepData>, timeStamp: Long): Int
}