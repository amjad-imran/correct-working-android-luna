package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup

interface OreoBodyTemperatureDataSource {

    suspend fun insertData(
        data: OreoBodyTemperatureBreakup
    ): Boolean

    suspend fun getTodayData(date: String): OreoBodyTemperatureBreakup?

    suspend fun getUnSyncServerData(
        endDate: Long, isSync: Boolean
    ): List<OreoBodyTemperatureBreakup>?

    suspend fun deleteOldData(timeStamp: Long): Int
    suspend fun updateServerSyncData(
        dataList: List<OreoBodyTemperatureBreakup>, timeStamp: Long
    ): Int

    suspend fun checkHalfSyncData()
}