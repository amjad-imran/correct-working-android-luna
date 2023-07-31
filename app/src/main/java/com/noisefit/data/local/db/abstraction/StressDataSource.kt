package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.models.StressDataBreakup

interface StressDataSource {

    suspend fun insertData(
        data: List<StressDataBreakup>
    ): Boolean

    suspend fun getTodayData(date: String): List<StressDataBreakup>?
    suspend fun getUnSyncServerData(endDate: Long,isSync:Boolean): List<StressDataBreakup>?
    suspend fun deleteOldData(timeStamp: Long): Int
    suspend fun updateServerSyncData(dataList: List<StressDataBreakup>, timeStamp: Long): Int
    suspend fun checkHalfSyncData()

    suspend fun getStressBetweenTimeStamp(startTimeStamp: Long, endTimeStamp: Long): List<StressDataBreakup>?
}