package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.SleepData

@Dao
interface SleepDao : BaseDao<SleepData> {
    @Query("SELECT * FROM sleep_data where date = :date ")
    fun getTodayData(date: String): List<SleepData>?

    @Query("SELECT * FROM sleep_data where is_google_fit_sync = :isSyncGoogleFit limit 500")
    fun getUnSyncGoogleFitTodayData(isSyncGoogleFit: Boolean): List<SleepData>?

    @Query("SELECT * FROM sleep_data where startTime = :startTime and endTime = :endTime")
    fun checkDataExist(startTime: String, endTime: String): List<SleepData>?

    @Query("UPDATE sleep_data SET is_google_fit_sync = :is_google_fit_sync WHERE id IN (:ids)")
    fun updateGoogleFitStatus(ids: List<Int>, is_google_fit_sync: Boolean)

    @Query("SELECT * FROM sleep_data where is_synced = :is_synced ")
    fun getServerUnSyncData(is_synced: Boolean): List<SleepData>?

    @Query("SELECT id,is_synced,is_google_fit_sync,startTime,endTime,startDate,endDate,availableSleepTypes,date,total,deep,light,sober,awake,remCount,breathQuality,sleepScore,startTimeStamp,endTimeStamp,sleep_array FROM sleep_data where  total > 0 and sync_date >= :endDate and sync_date > 0 and is_synced = :isSync ")
    fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<SleepData>?

    @Query("DELETE from sleep_data where sync_date <= :timeStamp")
    fun deleteOlderData(timeStamp: Long): Int

    @Query("UPDATE sleep_data SET is_synced = :is_synced WHERE id IN (:ids) and sync_date <= :timeStamp")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean, timeStamp: Long): Int
}