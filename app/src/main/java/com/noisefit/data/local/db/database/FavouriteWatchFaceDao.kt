package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.FavouriteWatchFace
import com.noisefit_commans.models.WatchFace


@Dao
interface FavouriteWatchFaceDao : BaseDao<FavouriteWatchFace> {

    @Query("SELECT * FROM favourite_watch_faces")
    fun getFavouriteWatchFaces(): List<FavouriteWatchFace>?


    @Query("DELETE FROM favourite_watch_faces")
    fun deleteAll()


    @Query("DELETE FROM favourite_watch_faces WHERE id = :watchFaceId")
    fun removeWatchFace(watchFaceId: Int)
}