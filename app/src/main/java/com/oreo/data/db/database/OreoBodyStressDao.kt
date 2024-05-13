package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBodyStressData
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.models.StressDataBreakup
import java.util.ArrayList

@Dao
interface OreoBodyStressDao : BaseDao<OreoBodyStressData> {
    @Query("SELECT * FROM body_stress where date = :date")
    fun getTodayData(date: String): OreoBodyStressData?


    @Query("UPDATE body_stress SET break_up = :breakUp,is_synced = :is_synced  WHERE date = :date")
    fun updateViaDate(breakUp: String, date: String, is_synced: Boolean)


    @Query("Delete FROM body_stress where date = :date")
    fun deleteTodayData(date: String)

    @Query("UPDATE body_stress SET is_google_fit_sync = :is_google_fit_sync WHERE id IN (:ids)")
    fun updateGoogleFitStatus(ids: List<Int>, is_google_fit_sync: Boolean)

    @Query("SELECT * FROM body_stress where is_synced = :is_synced")
    fun getServerUnSyncData(is_synced: Boolean): List<OreoBodyStressData>?

    @Query("UPDATE body_stress SET is_synced = :is_synced WHERE id IN (:ids)")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean): Int

    @Query("DELETE FROM body_stress WHERE date <= date('now', '-' || :day || ' days')")
    fun deleteOlderData(day: Int): Int
}