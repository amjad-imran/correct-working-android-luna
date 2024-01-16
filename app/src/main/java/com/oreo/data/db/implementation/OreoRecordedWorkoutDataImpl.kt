package com.oreo.data.db.implementation

import com.noisefit_commans.data.model.RecordedWorkoutData
import com.oreo.data.db.abstaction.OreoRecordedWorkoutDataSource
import com.oreo.data.db.database.OreoRecordedWorkoutDao
import javax.inject.Inject

class OreoRecordedWorkoutDataImpl
@Inject
constructor(
    private val oreoRecordedWorkoutDao: OreoRecordedWorkoutDao
) : OreoRecordedWorkoutDataSource {

    override suspend fun getAllWorkouts(): List<RecordedWorkoutData>? {
        return oreoRecordedWorkoutDao.getAllWorkouts()
    }

    override suspend fun deleteByStartTime(startTime: Long): Boolean {
        oreoRecordedWorkoutDao.deleteByStartTime(startTime)
        return true
    }

    override suspend fun deleteById(id: Int): Boolean {
        oreoRecordedWorkoutDao.deleteById(id)
        return true
    }

    override suspend fun insertData(data: List<RecordedWorkoutData>): Boolean {
        oreoRecordedWorkoutDao.insertAll(data)
        return true
    }

    override suspend fun deleteAllAutoSport(): Boolean {
        oreoRecordedWorkoutDao.deleteAllAutoSport()
        return true
    }


}