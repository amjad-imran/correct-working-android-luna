package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoStressDataBreakup


interface OreoStressDataSource {

    suspend fun insertData(
        data: OreoStressDataBreakup
    ): Boolean

    suspend fun getTodayData(date: String): OreoStressDataBreakup?
    suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoStressDataBreakup>?
    suspend fun deleteOldData(days: Int): Int
    suspend fun updateServerSyncData(dataList: List<OreoStressDataBreakup>, timeStamp: Long): Int
    suspend fun checkHalfSyncData()

    suspend fun getStressBetweenTimeStamp(
        startTimeStamp: Long,
        endTimeStamp: Long
    ): List<Int>
}