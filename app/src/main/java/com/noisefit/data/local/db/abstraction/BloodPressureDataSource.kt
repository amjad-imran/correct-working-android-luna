package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.models.BloodPressureData

interface BloodPressureDataSource {
    suspend fun insertOrUpdate(data: BloodPressureData)
    suspend fun insertOrDelete(data: BloodPressureData)
    suspend fun getTodayData(date: String): BloodPressureData
}