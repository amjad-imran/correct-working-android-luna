package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.HeartRate
import com.noisefit_commans.models.StressDataBreakup


@Dao
interface HeartRateDao : BaseDao<HeartRate> {
    @Query("SELECT * FROM heart_rate where date = :date and averageHeartRate > 0 group by time")
    fun getTodayData(date: String): List<HeartRate>?

    @Query("SELECT * FROM heart_rate where sync_date > :startTimeStamp and sync_date < :endTimeStamp group by sync_date")
    fun getHeartRateBetweenTimeStamp(startTimeStamp: Long, endTimeStamp: Long): List<HeartRate>?

    @Query("SELECT * FROM heart_rate where  is_google_fit_sync = :isSyncGoogleFit and averageHeartRate > 0  group by time limit 500")
    fun getUnSyncGoogleFitTodayData(isSyncGoogleFit: Boolean): List<HeartRate>?

    @Query("Delete FROM heart_rate where date = :date")
    fun deleteTodayData(date: String)

    @Query("UPDATE heart_rate SET is_google_fit_sync = :is_google_fit_sync WHERE id IN (:ids)")
    fun updateGoogleFitStatus(ids: List<Int>, is_google_fit_sync: Boolean)

    @Query("SELECT * FROM heart_rate where is_synced = :is_synced and averageHeartRate > 0 ")
    fun getServerUnSyncData(is_synced: Boolean): List<HeartRate>?

    @Query("SELECT id,resetData,is_google_fit_sync,is_synced,averageHeartRate,highestHeartRate,lowestHeartRate,restingHeartRate,time,date FROM heart_rate where  averageHeartRate > 0 and sync_date >= :endDate and sync_date > 0 and is_synced = :isSync order by sync_date ASC ")
    fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<HeartRate>?

    @Query("DELETE from heart_rate WHERE id IN (:ids) and sync_date <= :timeStamp")
    fun deleteServerSyncData(ids: List<Int>, timeStamp: Long): Int

    @Query("DELETE from heart_rate where sync_date<=:timeStamp")
    fun deleteOlderData(timeStamp: Long): Int

    @Query("UPDATE heart_rate SET is_synced = :is_synced WHERE id IN (:ids) and sync_date <= :timeStamp")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean, timeStamp: Long): Int

    @Query("SELECT * FROM heart_rate where is_synced = :is_synced and averageHeartRate>0 group by date")
    fun checkHalfSyncData(is_synced: Boolean): List<StressDataBreakup>?

    @Query("UPDATE heart_rate SET is_synced = :is_synced WHERE date IN (:date)")
    fun updateHalfSyncData(is_synced: Boolean, date: List<String>): Int
}