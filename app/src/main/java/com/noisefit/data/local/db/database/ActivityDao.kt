package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.SportsModeResponse

@Dao
interface ActivityDao : BaseDao<SportsModeResponse> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertActivity(sportsModeResponse: SportsModeResponse)

    @Query("SELECT * FROM activities")
    fun getActivities(): List<SportsModeResponse>

    @Query("SELECT * FROM activities WHERE is_synced = 0")
    fun getUnSyncedActivities(): List<SportsModeResponse>

    @Query("UPDATE activities SET is_synced = 1 WHERE is_synced = 0")
    fun setActivitiesSynced()

    @Query("DELETE FROM activities")
    fun deleteActivities()
}