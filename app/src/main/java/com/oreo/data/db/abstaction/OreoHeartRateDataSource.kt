package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoHeartRate


interface OreoHeartRateDataSource {

    suspend fun insertData(
        data: OreoHeartRate
    ): Boolean

    suspend fun getTodayData(date: String): OreoHeartRate?
    suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoHeartRate>?
    suspend fun deleteOldData(days: Int): Int
    suspend fun updateServerSyncData(dataList: List<OreoHeartRate>, timeStamp: Long): Int
    suspend fun checkHalfSyncData()

    suspend fun getHeartRateBetweenTimeStamp(
        startTimeStamp: Long,
        endTimeStamp: Long
    ): List<Int>
}