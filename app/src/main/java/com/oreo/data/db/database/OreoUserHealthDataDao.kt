package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.UserHealthData

@Dao
interface OreoUserHealthDataDao : BaseDao<UserHealthData> {
    @Query("SELECT * FROM user_health_data where date = :date")
    fun getByDate(date: String): UserHealthData?

    @Query("UPDATE user_health_data SET userHealthData = :userHealthData,trendData = :trendData WHERE date = :date")
    fun updateViaDate(
        userHealthData: String?,
        trendData: String?,
        date: String
    )

    @Query("Delete FROM user_health_data")
    fun clearAllData()

    @Query("Delete FROM user_health_data WHERE date=:date")
    fun clearByDate(date: String)


    @Query("SELECT trendData FROM user_health_data where date = :date")
    fun getTodayTrend(date: String): String?

    @Query("SELECT impact FROM user_health_data where date = :date")
    fun getTodayImpact(date: String): String?

    /*@Query("Delete FROM day_time_movement where date = :date")
    fun deleteTodayData(date: String)

    @Query("UPDATE day_time_movement SET is_google_fit_sync = :is_google_fit_sync WHERE id IN (:ids)")
    fun updateGoogleFitStatus(ids: List<Int>, is_google_fit_sync: Boolean)

    @Query("SELECT * FROM day_time_movement where is_synced = :is_synced")
    fun getServerUnSyncData(is_synced: Boolean): List<DayTimeMovementBreakup>?

    @Query("DELETE FROM day_time_movement WHERE date <= date('now', '-' || :day || ' days')")
    fun deleteOlderData(day: Int): Int*/
}