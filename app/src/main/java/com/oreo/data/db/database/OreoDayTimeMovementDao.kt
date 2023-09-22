package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.models.StressDataBreakup

@Dao
interface OreoDayTimeMovementDao : BaseDao<DayTimeMovementBreakup> {
    @Query("SELECT * FROM day_time_movement where date = :date")
    fun getTodayData(date: String): DayTimeMovementBreakup?


    @Query("UPDATE day_time_movement SET break_up = :breakUp,is_synced = :is_synced  WHERE date = :date")
    fun updateViaDate(breakUp: String, date: String, is_synced: Boolean)


    @Query("Delete FROM day_time_movement where date = :date")
    fun deleteTodayData(date: String)

    @Query("UPDATE day_time_movement SET is_google_fit_sync = :is_google_fit_sync WHERE id IN (:ids)")
    fun updateGoogleFitStatus(ids: List<Int>, is_google_fit_sync: Boolean)

    @Query("SELECT * FROM day_time_movement where is_synced = :is_synced")
    fun getServerUnSyncData(is_synced: Boolean): List<DayTimeMovementBreakup>?

    @Query("DELETE FROM day_time_movement WHERE date <= date('now', '-' || :day || ' days')")
    fun deleteOlderData(day: Int): Int
}