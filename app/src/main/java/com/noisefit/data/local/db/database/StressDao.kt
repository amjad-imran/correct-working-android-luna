package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.StressDataBreakup

@Dao
interface StressDao : BaseDao<StressDataBreakup> {
    @Query("SELECT * FROM stress_data where date = :date and value>0 group by time")
    fun getTodayData(date: String): List<StressDataBreakup>?

    @Query("SELECT * FROM stress_data where sync_date > :startTimeStamp and sync_date < :endTimeStamp group by sync_date")
    fun getStressBetweenTimeStamp(startTimeStamp: Long, endTimeStamp: Long): List<StressDataBreakup>?

    @Query("SELECT * FROM stress_data where is_synced = :is_synced and value>0 group by time")
    fun getServerUnSyncData(is_synced: Boolean): List<StressDataBreakup>?

    @Query("SELECT id,is_synced,value,resetData,time,date FROM stress_data where  value > 0 and sync_date >= :endDate and sync_date > 0 and is_synced = :isSync order by sync_date ASC")
    fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<StressDataBreakup>?

    @Query("DELETE from stress_data where sync_date<=:timeStamp")
    fun deleteOlderData(timeStamp: Long): Int

    @Query("UPDATE stress_data SET is_synced = :is_synced WHERE id IN (:ids) and sync_date <= :timeStamp")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean, timeStamp: Long): Int

    @Query("SELECT * FROM stress_data where is_synced = :is_synced and value>0 group by date")
    fun checkHalfSyncData(is_synced: Boolean): List<StressDataBreakup>?

    @Query("UPDATE stress_data SET is_synced = :is_synced WHERE date IN (:date)")
    fun updateHalfSyncData(is_synced: Boolean, date: List<String>): Int
}