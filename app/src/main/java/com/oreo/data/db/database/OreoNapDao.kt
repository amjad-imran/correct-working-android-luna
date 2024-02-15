package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.OreoNapData

@Dao
interface OreoNapDao : BaseDao<OreoNapData> {
    @Query("SELECT * FROM nap_data where date = :date ")
    fun getTodayData(date: String): List<OreoNapData>?

    @Query("SELECT * FROM nap_data")
    fun getAllData(): List<OreoNapData>?

    @Query("DELETE from nap_data where id=:id")
    fun removeNapById(id: Int)

    @Query("DELETE FROM nap_data WHERE date <= date('now', '-' || :day || ' days')")
    fun deleteOlderData(day: Int): Int

}