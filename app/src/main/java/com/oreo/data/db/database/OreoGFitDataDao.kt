package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.GoogleFitDataDb
import com.noisefit_commans.data.model.GoogleFitWorkoutData


@Dao
interface OreoGFitDataDao : BaseDao<GoogleFitDataDb> {

    @Query("SELECT * FROM google_fit_data WHERE type = :type")
    fun getDataByType(type: String): List<GoogleFitDataDb>?

    @Query("DELETE FROM google_fit_data WHERE type = :type")
    fun removeDataByType(type: String)

    @Query("SELECT * FROM google_fit_data WHERE is_synced = 0")
    fun getUnSyncedData(): List<GoogleFitDataDb>

    @Query("UPDATE google_fit_data SET is_synced = 1 WHERE id = :id")
    fun markDataSynced(id: Int)

    @Query("UPDATE google_fit_data SET is_synced = 1 WHERE type = :type")
    fun markDataSyncedByType(type:String)

    @Query("DELETE FROM google_fit_data WHERE startTime < :timestamp")
    fun deleteData(timestamp: Long)
}
