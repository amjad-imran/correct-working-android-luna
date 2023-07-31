package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.models.StressDataBreakup
import java.util.ArrayList

@Dao
interface OreoHeartRateDao : BaseDao<OreoHeartRate> {
    @Query("SELECT * FROM heart_rate where date = :date")
    fun getTodayData(date: String): OreoHeartRate?


    @Query("UPDATE heart_rate SET break_up = :breakUp,is_synced = :is_synced WHERE date = :date")
    fun updateViaDate(breakUp: String, date: String, is_synced: Boolean)


    @Query("Delete FROM heart_rate where date = :date")
    fun deleteTodayData(date: String)

    @Query("UPDATE heart_rate SET is_google_fit_sync = :is_google_fit_sync WHERE id IN (:ids)")
    fun updateGoogleFitStatus(ids: List<Int>, is_google_fit_sync: Boolean)

    @Query("SELECT * FROM heart_rate where is_synced = :is_synced")
    fun getServerUnSyncData(is_synced: Boolean): List<OreoHeartRate>?

    @Query("UPDATE heart_rate SET is_synced = :is_synced WHERE id IN (:ids)")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean): Int

}