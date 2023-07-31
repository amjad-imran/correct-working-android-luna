package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoRespiratoryData

interface OreoRespiratoryDataSource {
    suspend fun insertData(
        data: OreoRespiratoryData
    ): Boolean

    suspend fun getTodayData(date: String): List<OreoRespiratoryData>?
    suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoRespiratoryData>?
    suspend fun deleteOldData(timeStamp: Long): Int
    suspend fun updateServerSyncData(dataList: List<OreoRespiratoryData>, timeStamp: Long): Int
    suspend fun checkHalfSyncData()
}