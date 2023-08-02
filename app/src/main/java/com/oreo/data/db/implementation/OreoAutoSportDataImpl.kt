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

    override suspend fun getAllNotAcceptingData(): List<OreoAutoSportData>? {
        return oreoAutoSportDao.getAllNotAcceptingData(false)
    }

    override suspend fun insertData(data: List<OreoAutoSportData>): Boolean {
        oreoAutoSportDao.insertAll(data)
        return true
    }

    override suspend fun deleteAllAutoSport(): Boolean {
        oreoAutoSportDao.deleteAllAutoSport(false)
        return true
    }

    override suspend fun deleteAutoSport(id: Int): Boolean {
        oreoAutoSportDao.deleteAutoSport(id)
        return true
    }


}