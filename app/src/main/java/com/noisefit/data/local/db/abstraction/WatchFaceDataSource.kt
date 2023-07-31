package com.noisefit.data.local.db.abstraction

import com.noisefit.data.remote.response.CatWiseWatchFacesItem
import com.noisefit.data.remote.response.WatchFaceCustomListResponse
import com.noisefit_commans.models.FavouriteWatchFace
import com.noisefit_commans.models.WatchFace


interface WatchFaceDataSource {

    suspend fun insertData(
        data: List<WatchFace>
    ): List<WatchFace>?


    suspend fun insertFavouriteData(
        data: List<FavouriteWatchFace>
    ): List<FavouriteWatchFace>?

    suspend fun getWatchFaces(): List<WatchFace>?

    suspend fun getCustomWatchFaceData(removeData: Boolean): WatchFaceCustomListResponse?

    suspend fun setCustomWatchFaceData(data: WatchFaceCustomListResponse?)

    suspend fun getWatchFaceCategories(removeData: Boolean): List<CatWiseWatchFacesItem>?

    suspend fun setWatchFaceCategories(data: List<CatWiseWatchFacesItem>?)


    suspend fun getWatchFacesByCategory(watchfaceCatId: Int): List<WatchFace>?
    suspend fun deleteWatchFacesByCategoryId(watchfaceCatId: Int)

    suspend fun deleteOldWatchFaces()

    suspend fun getWatchFaceById(watchFaceId: Int): WatchFace?

    suspend fun setFavourite(watchFaceId: Int, isFavourite: String)

    suspend fun deleteFavouriteWatchFaces()

    suspend fun getFavouriteWatchFaces(isForceRefresh: Boolean): List<WatchFace>?

    suspend fun addToFavourites(watchFace: FavouriteWatchFace)

    suspend fun removeFromFavourites(watchFaceId: Int)
}