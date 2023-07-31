package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.BloodOxygenBreakup
import com.noisefit_commans.models.StressDataBreakup


@Dao
interface BloodOxygenDao : BaseDao<BloodOxygenBreakup> {
    @Query("SELECT * FROM blood_oxygen where date = :date and value>0 group by time")
    fun getTodayData(date: String): List<BloodOxygenBreakup>?

    @Query("SELECT * FROM blood_oxygen where is_synced = :is_synced and value>0")
    fun getServerUnSyncData(is_synced: Boolean): List<BloodOxygenBreakup>?

    @Query("SELECT id,is_synced,value,resetData,time,date FROM blood_oxygen where value > 0  and sync_date >= :endDate and sync_date > 0 and is_synced = :isSync order by sync_date ASC")
    fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<BloodOxygenBreakup>?

    @Query("DELETE from blood_oxygen WHERE id IN (:ids) and sync_date <= :timeStamp")
    fun deleteServerSyncData(ids: List<Int>, timeStamp: Long): Int

    @Query("DELETE from blood_oxygen where sync_date <= :timeStamp")
    fun deleteOlderData(timeStamp: Long): Int

    @Query("UPDATE blood_oxygen SET is_synced = :is_synced WHERE id IN (:ids) and sync_date <= :timeStamp")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean, timeStamp: Long): Int

    @Query("SELECT * FROM blood_oxygen where is_synced = :is_synced and value>0 group by date")
    fun checkHalfSyncData(is_synced: Boolean): List<StressDataBreakup>?

    @Query("UPDATE blood_oxygen SET is_synced = :is_synced WHERE date IN (:date)")
    fun updateHalfSyncData(is_synced: Boolean, date: List<String>): Int
}