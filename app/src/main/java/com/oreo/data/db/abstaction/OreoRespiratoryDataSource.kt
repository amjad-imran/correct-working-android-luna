package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoRespiratoryData

interface OreoRespiratoryDataSource {
    suspend fun insertData(
        data: OreoRespiratoryData
    ): Boolean

    suspend fun getTodayData(date: String): OreoRespiratoryData?
    suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoRespiratoryData>?
    suspend fun deleteOldData(days: Int): Int
    suspend fun updateServerSyncData(dataList: List<OreoRespiratoryData>, timeStamp: Long): Int
    suspend fun checkHalfSyncData()
    suspend fun getDataBetweenTimeStamp(startTimeStamp: Long, endTimeStamp: Long): List<Int>
}