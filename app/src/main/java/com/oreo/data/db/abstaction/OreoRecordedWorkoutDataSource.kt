package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.RecordedWorkoutData

interface OreoRecordedWorkoutDataSource {

    suspend fun getAllWorkouts(): List<RecordedWorkoutData>?
    suspend fun deleteByStartTime(startTime: Long): Boolean
    suspend fun deleteById(id: Int): Boolean

    suspend fun insertData(
        data: List<RecordedWorkoutData>
    ): Boolean

    suspend fun deleteAllAutoSport(): Boolean
}