package com.noisefit.data.local.db.implementation

import com.noisefit.data.local.db.abstraction.BloodPressureDataSource
import com.noisefit.data.local.db.database.BloodPressureDao
import com.noisefit_commans.models.BloodPressureData
import javax.inject.Inject


class BloodPressureDataImpl
@Inject
constructor(
    private val bloodPressureDao: BloodPressureDao
) : BloodPressureDataSource {
    override suspend fun insertOrUpdate(data: BloodPressureData) {
        
    }

    override suspend fun insertOrDelete(data: BloodPressureData) {
        
    }

    override suspend fun getTodayData(date: String): BloodPressureData{
        return bloodPressureDao.getTodayData()
    }


}
