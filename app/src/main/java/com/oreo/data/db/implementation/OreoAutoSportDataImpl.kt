package com.oreo.data.db.implementation

import com.noisefit_commans.data.model.OreoAutoSportData
import com.oreo.data.db.abstaction.OreoAutoSportDataSource
import com.oreo.data.db.database.OreoAutoSportDao
import javax.inject.Inject

class OreoAutoSportDataImpl
@Inject
constructor(
    private val oreoAutoSportDao: OreoAutoSportDao
) : OreoAutoSportDataSource {
    override suspend fun deleteOldData(time: Long): Boolean {
        oreoAutoSportDao.deleteOlderData(time)
        return true
    }

    override suspend fun getAllNotAcceptingData(startTimeStamp:Long): List<OreoAutoSportData>? {
        return oreoAutoSportDao.getAllNotAcceptingData(false,0,startTimeStamp)
    }

    override suspend fun getWorkoutByTime(timeStamp:Long): OreoAutoSportData? {
        return oreoAutoSportDao.getWorkoutByTime(timeStamp)
    }

    override suspend fun insertData(data: List<OreoAutoSportData>): Boolean {
        oreoAutoSportDao.insertAll(data)
        return true
    }

    override suspend fun markWorkoutSyncedAll(): Boolean {
        oreoAutoSportDao.markWorkoutSyncedAll(1)
        return true
    }

    override suspend fun deleteAutoSport(id: Int): Boolean {
        oreoAutoSportDao.deleteAutoSport(id)
        return true
    }
    override suspend fun markWorkoutSynced(id: Int): Boolean {
        oreoAutoSportDao.markWorkoutSynced(1,id)
        return true
    }


}