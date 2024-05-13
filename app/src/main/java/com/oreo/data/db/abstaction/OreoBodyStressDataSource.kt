package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoBodyStressData
import com.noisefit_commans.data.model.OreoRespiratoryData

interface OreoBodyStressDataSource {
    suspend fun insertData(
        data: OreoBodyStressData
    ): Boolean

    suspend fun getTodayData(date: String): OreoBodyStressData?
    suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoBodyStressData>?
    suspend fun deleteOldData(days: Int): Int
    suspend fun updateServerSyncData(dataList: List<OreoBodyStressData>, timeStamp: Long): Int
    suspend fun checkHalfSyncData()
    suspend fun getDataBetweenTimeStamp(startTimeStamp: Long, endTimeStamp: Long): List<Int>
}