package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.WatchFace


@Dao
interface WatchFaceDao : BaseDao<WatchFace> {
    @Query("SELECT * FROM watch_faces")
    fun getWatchFaces(): List<WatchFace>?

    @Query("SELECT * FROM watch_faces")
    fun getWatchFacesByCategory(): List<WatchFace>?

    @Query("SELECT * from watch_faces where watchfaceCatId = :watchfaceCatId")
    fun getWatchFacesByCategoryId(watchfaceCatId: Int): List<WatchFace>?

    @Query("SELECT * from watch_faces where id = :watchFaceId")
    fun getWatchFaceById(watchFaceId: Int): WatchFace?

    @Query("UPDATE watch_faces SET is_favourite = :isFavourite WHERE id=:watchFaceId")
    fun setFavourite(watchFaceId: Int, isFavourite: String)

    @Query("DELETE FROM watch_faces")
    fun deleteOlderWatchFaces()

    @Query("DELETE FROM watch_faces WHERE watchfaceCatId=:watchfaceCatId")
    fun deleteWatchFacesByCategory(watchfaceCatId: Int)
}