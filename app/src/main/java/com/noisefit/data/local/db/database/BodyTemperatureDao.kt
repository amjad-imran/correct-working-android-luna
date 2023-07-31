package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.BodyTemperatureBreakup
import com.noisefit_commans.models.StressDataBreakup


@Dao
interface BodyTemperatureDao : BaseDao<BodyTemperatureBreakup> {
    @Query("SELECT * FROM body_temperature where date = :date and value>0 group by time")
    fun getTodayData(date: String): List<BodyTemperatureBreakup>?

    @Query("SELECT * FROM body_temperature where is_synced = :is_synced and value>0 group by time")
    fun getServerUnSyncData(is_synced: Boolean): List<BodyTemperatureBreakup>?

    @Query("SELECT id,is_synced,value,time,date,resetData FROM body_temperature where  value > 0 and sync_date >= :endDate and sync_date > 0 and is_synced = :isSync order by sync_date ASC")
    fun getUnSyncServerData(endDate: Long,isSync:Boolean): List<BodyTemperatureBreakup>?

    @Query("DELETE from body_temperature WHERE id IN (:ids) and sync_date <= :timeStamp")
    fun deleteServerSyncData(ids: List<Int>, timeStamp: Long): Int

    @Query("DELETE from body_temperature where sync_date<=:timeStamp")
    fun deleteOlderData(timeStamp: Long): Int

    @Query("UPDATE body_temperature SET is_synced = :is_synced WHERE id IN (:ids) and sync_date <= :timeStamp")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean, timeStamp: Long):Int

    @Query("SELECT * FROM body_temperature where is_synced = :is_synced and value>0 group by date")
    fun checkHalfSyncData(is_synced: Boolean): List<StressDataBreakup>?

    @Query("UPDATE body_temperature SET is_synced = :is_synced WHERE date IN (:date)")
    fun updateHalfSyncData(is_synced: Boolean, date: List<String>): Int
}