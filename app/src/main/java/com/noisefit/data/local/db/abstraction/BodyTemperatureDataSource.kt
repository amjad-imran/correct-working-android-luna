package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.models.BodyTemperatureBreakup


interface BodyTemperatureDataSource {

    suspend fun insertData(
        data: List<BodyTemperatureBreakup>
    ): Boolean

    suspend fun getTodayData(date: String): List<BodyTemperatureBreakup>?

    suspend fun getUnSyncServerData(endDate:Long,isSync:Boolean): List<BodyTemperatureBreakup>?
    suspend fun deleteOldData(timeStamp: Long): Int
    suspend fun updateServerSyncData(dataList: List<BodyTemperatureBreakup>, timeStamp: Long): Int
    suspend fun checkHalfSyncData()
}