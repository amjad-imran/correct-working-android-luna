package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.models.StressDataBreakup
import java.util.ArrayList

@Dao
interface OreoBodyTemperatureDao : BaseDao<OreoBodyTemperatureBreakup> {
    @Query("SELECT * FROM body_temperature where date = :date")
    fun getTodayData(date: String): OreoBodyTemperatureBreakup?

    @Query("SELECT * FROM body_temperature where is_synced = :is_synced")
    fun getServerUnSyncData(is_synced: Boolean): List<OreoBodyTemperatureBreakup>?

    @Query("UPDATE body_temperature SET break_up = :breakUp,is_synced = :is_synced  WHERE date = :date")
    fun updateViaDate(breakUp: String, date: String, is_synced: Boolean)


    @Query("UPDATE body_temperature SET is_synced = :is_synced WHERE id IN (:ids)")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean): Int

}