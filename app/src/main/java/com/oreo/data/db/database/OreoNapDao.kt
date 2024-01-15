package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.OreoNapData

@Dao
interface OreoNapDao : BaseDao<OreoNapData> {
    @Query("SELECT * FROM nap_data where date = :date ")
    fun getTodayData(date: String): List<OreoNapData>?


}