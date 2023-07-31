package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.BloodPressureData


@Dao
interface BloodPressureDao : BaseDao<BloodPressureData> {
    @Query("SELECT * FROM blood_pressure")
    fun getTodayData(): BloodPressureData
}