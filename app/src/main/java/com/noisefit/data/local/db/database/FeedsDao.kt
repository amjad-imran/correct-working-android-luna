package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit.data.model.FeedsDbValue


@Dao
interface FeedsDao : BaseDao<FeedsDbValue> {
    @Query("SELECT * FROM feeds where type = :type")
    fun getFeeds(type: String): List<FeedsDbValue>?

    @Query("SELECT * FROM feeds where type = :type AND key =:key")
    fun getFeedsById(type: String, key: Long): FeedsDbValue?

    @Query("DELETE FROM feeds")
    fun deleteAllFeeds()

    @Query("DELETE FROM feeds WHERE key = :key AND type = :type")
    fun deleteByKey(key: String, type: String)

    @Query("DELETE FROM feeds WHERE type = :type")
    fun deleteByType(type: String)

    @Query("UPDATE feeds SET value =:value WHERE key =:postId")
    fun updateFeedById(postId: Long, value: String)
}

enum class FeedsType {
    TIMELINE
}