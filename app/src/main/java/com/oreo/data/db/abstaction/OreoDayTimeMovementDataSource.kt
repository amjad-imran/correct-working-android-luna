package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.DayTimeMovementBreakup

interface OreoDayTimeMovementDataSource {
    suspend fun insertData(
        data: DayTimeMovementBreakup
    ): Boolean

    suspend fun getTodayData(date: String): List<DayTimeMovementBreakup>?
    suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<DayTimeMovementBreakup>?
    suspend fun deleteOldData(timeStamp: Long): Int
    suspend fun updateServerSyncData(dataList: List<DayTimeMovementBreakup>, timeStamp: Long): Int
    suspend fun checkHalfSyncData()
}