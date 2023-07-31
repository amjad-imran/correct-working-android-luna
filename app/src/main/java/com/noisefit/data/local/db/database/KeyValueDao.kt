package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.KeyValue


@Dao
interface KeyValueDao : BaseDao<KeyValue> {
    @Query("SELECT * FROM key_value where key = :key AND type = :type")
    fun getData(key: String, type: String): KeyValue?

    @Query("DELETE FROM key_value")
    fun deleteAllData()

    @Query("DELETE FROM key_value WHERE key = :key AND type = :type")
    fun deleteByKey(key: String, type: String)

    @Query("DELETE FROM key_value WHERE type = :type")
    fun deleteByType(type: String)
}