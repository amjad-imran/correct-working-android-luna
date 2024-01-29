package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.GoogleFitWorkoutData


@Dao
interface OreoGFitWorkoutDao : BaseDao<GoogleFitWorkoutData> {
    @Query("SELECT * FROM google_fit_workout where is_synced = :isSyncGoogleFit limit 500")
    fun getUnSyncGoogleFitTodayData(isSyncGoogleFit: Boolean): List<GoogleFitWorkoutData>?

    @Query("UPDATE google_fit_workout SET is_synced = :is_synced  WHERE id = :id")
    fun updateData(id: Int, is_synced: Boolean)

    @Query("UPDATE google_fit_workout SET is_synced = :is_synced WHERE id IN (:ids) ")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean): Int
}
