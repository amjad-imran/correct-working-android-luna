package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.UserHealthData

interface OreoUserHealthDataDataSource {
    suspend fun insertData(
        data: UserHealthData
    ): Boolean

    suspend fun getDataByDate(date: String): UserHealthData?

    suspend fun clearAllData(): Boolean

    suspend fun clearDataByDates(dates: List<String>)

    fun getTodayTrend(): String?

    fun getTodayImpact(): String?

    /*  suspend fun getTodayDayTimeMovement(date: String): String?
      suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<DayTimeMovementBreakup>?
      suspend fun deleteOldData(days: Int): Int
      suspend fun updateServerSyncData(dataList: List<DayTimeMovementBreakup>, timeStamp: Long): Int
      suspend fun checkHalfSyncData()*/
}