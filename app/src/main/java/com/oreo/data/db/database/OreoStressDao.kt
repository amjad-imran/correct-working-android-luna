package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.OreoStressDataBreakup


@Dao
interface OreoStressDao : BaseDao<OreoStressDataBreakup> {
    @Query("SELECT * FROM stress_data where date = :date")
    fun getTodayData(date: String): OreoStressDataBreakup?

    @Query("UPDATE stress_data SET break_up = :breakUp,is_synced = :is_synced WHERE date = :date")
    fun updateViaDate(breakUp: String, date: String, is_synced: Boolean)


    @Query("SELECT * FROM stress_data where is_synced = :is_synced")
    fun getServerUnSyncData(is_synced: Boolean): List<OreoStressDataBreakup>?


    @Query("UPDATE stress_data SET is_synced = :is_synced WHERE id IN (:ids)")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean): Int

}