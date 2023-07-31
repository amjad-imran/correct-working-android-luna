package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.GoogleFitData

@Dao
interface GoogleFitDao: BaseDao<GoogleFitData> {
    @Query("SELECT * FROM google_fit where date = :date")
    fun getTodayData(date: String): GoogleFitData?

    @Query("Delete FROM google_fit where date = :date")
    fun deleteTodayData(date: String)

    @Query("Update google_fit set steps = :steps, steps_last_sync = :stepsLastSync,is_synced = :isSynced where date = :date")
    fun updateSteps(
        steps: Int,
        stepsLastSync: Long,
        isSynced: Boolean,
        date: String
    )
}