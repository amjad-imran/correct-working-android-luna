package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoSleepData

interface OreoSleepDataSource {

    suspend fun insertData(
        data: OreoSleepData
    ): Boolean

    suspend fun getTodayData(date: String): List<OreoSleepData>?
    suspend fun getServerUnSyncData(): List<OreoSleepData>?

    suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoSleepData>?
    suspend fun deleteOldData(days: Int): Int
    suspend fun updateServerSyncData(dataList: List<OreoSleepData>, timeStamp: Long): Int
    suspend fun setHealthScore(score: Int, date: String)

    suspend fun getUnSyncGoogleFitData(): List<OreoSleepData>?
    suspend fun updateUnSyncGoogleFitData(data: OreoSleepData)

}