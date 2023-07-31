package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.models.HeartRate

interface HeartRateDataSource {

    suspend fun insertData(
        data: List<HeartRate>
    ): Boolean

    suspend fun getTodayData(date: String): List<HeartRate>?
    suspend fun getUnSyncServerData(endDate:Long,isSync:Boolean): List<HeartRate>?
    suspend fun deleteOldData(timeStamp: Long): Int
    suspend fun updateServerSyncData(dataList: List<HeartRate>,timeStamp: Long): Int
    suspend fun checkHalfSyncData()

    suspend fun getHeartRateBetweenTimeStamp(startTimeStamp: Long, endTimeStamp: Long): List<HeartRate>?
}