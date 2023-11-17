package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.UserHealthData

@Dao
interface OreoUserHealthDataDao : BaseDao<UserHealthData> {
    @Query("SELECT * FROM user_health_data where date = :date")
    fun getByDate(date: String): UserHealthData?

    @Query("UPDATE user_health_data SET dashboard = :dashboard,sleep = :sleep,activity = :activity,readiness = :readiness  WHERE date = :date")
    fun updateViaDate(
        dashboard: String?,
        sleep: String?,
        activity: String?,
        readiness: String?,
        date: String
    )

    /*@Query("Delete FROM day_time_movement where date = :date")
    fun deleteTodayData(date: String)

    @Query("UPDATE day_time_movement SET is_google_fit_sync = :is_google_fit_sync WHERE id IN (:ids)")
    fun updateGoogleFitStatus(ids: List<Int>, is_google_fit_sync: Boolean)

    @Query("SELECT * FROM day_time_movement where is_synced = :is_synced")
    fun getServerUnSyncData(is_synced: Boolean): List<DayTimeMovementBreakup>?

    @Query("DELETE FROM day_time_movement WHERE date <= date('now', '-' || :day || ' days')")
    fun deleteOlderData(day: Int): Int*/
}