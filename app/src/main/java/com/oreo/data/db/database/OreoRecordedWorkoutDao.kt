package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.data.model.RecordedWorkoutData

@Dao
interface OreoRecordedWorkoutDao : BaseDao<RecordedWorkoutData> {

    @Query("SELECT * FROM recorded_workout")
    fun getAllWorkouts(): List<RecordedWorkoutData>?


    @Query("Delete FROM recorded_workout where startTime = :startTime")
    fun deleteByStartTime(startTime: Long)

    @Query("Delete FROM recorded_workout where id = :id")
    fun deleteById(id: Int)


    @Query("Delete FROM recorded_workout")
    fun deleteAllAutoSport()

}