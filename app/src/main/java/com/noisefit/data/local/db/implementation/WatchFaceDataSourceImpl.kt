package com.noisefit.data.local.db.implementation

import androidx.room.Transaction
import com.noisefit.data.local.db.abstraction.WatchFaceDataSource
import com.noisefit.data.local.db.database.FavouriteWatchFaceDao
import com.noisefit.data.local.db.database.WatchFaceDao
import com.noisefit.data.remote.response.CatWiseWatchFacesItem
import com.noisefit.data.remote.response.WatchFaceCustomListResponse
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.FavouriteWatchFace

import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanN
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanNDays
import javax.inject.Inject


class WatchFaceDataSourceImpl
@Inject
constructor(
    private val watchFaceDao: WatchFaceDao,
    private val localDataStore: DataStoredInterface,
    private val favouritesWatchFaceDao: FavouriteWatchFaceDao
) : WatchFaceDataSource {

    @Transaction
    override suspend fun insertData(data: List<WatchFace>): List<WatchFace> {

        if (data.isNullOrEmpty()) {
            return data
        }
        watchFaceDao.insertAll(data)

        return data
    }

    @Transaction
    override suspend fun insertFavouriteData(data: List<FavouriteWatchFace>): List<FavouriteWatchFace>? {
        if (data.isEmpty()) {
            return data
        }
        favouritesWatchFaceDao.insertAll(data)
        return data
    }


    override suspend fun getWatchFaces(): List<WatchFace>? {
        val watchFaceList = watchFaceDao.getWatchFaces()

        if (!watchFaceList.isNullOrEmpty()) {
            if (watchFaceList[0].lastSync!!.checkTimeDifferenceMoreThanN(1)) {
                watchFaceDao.deleteOlderWatchFaces()
                return null
            }
        }
        return watchFaceList
    }

    override suspend fun getCustomWatchFaceData(removeData: Boolean): WatchFaceCustomListResponse? {
        if (removeData) {
            localDataStore.setCustomWatchFaceData(null)
            return null
        }
        return localDataStore.getCustomWatchFaceData()
    }

    override suspend fun setCustomWatchFaceData(data: WatchFaceCustomListResponse?) {
        localDataStore.setCustomWatchFaceData(data)
    }

    override suspend fun getWatchFaceCategories(removeData: Boolean): List<CatWiseWatchFacesItem>? {
        if (removeData) {
            localDataStore.setWatchFaceCategories(null)
            return null
        }
        return localDataStore.getWatchFaceCategories()
    }

    override suspend fun setWatchFaceCategories(data: List<CatWiseWatchFacesItem>?) {
        localDataStore.setWatchFaceCategories(data)
    }

    override suspend fun getWatchFacesByCategory(watchfaceCatId: Int): List<WatchFace>? {
        val watchFaceList = watchFaceDao.getWatchFacesByCategoryId(watchfaceCatId)
        if (!watchFaceList.isNullOrEmpty()) {
            if (watchFaceList[0].lastSync!!.checkTimeDifferenceMoreThanNDays(1)) {
                watchFaceDao.deleteWatchFacesByCategory(watchfaceCatId)
                return null
            }
        }
        return watchFaceList
    }

    override suspend fun deleteWatchFacesByCategoryId(watchfaceCatId: Int) {
        watchFaceDao.deleteWatchFacesByCategory(watchfaceCatId)
    }

    override suspend fun deleteOldWatchFaces() {
        return watchFaceDao.deleteOlderWatchFaces()
    }

    override suspend fun getWatchFaceById(watchFaceId: Int): WatchFace? {
        return watchFaceDao.getWatchFaceById(watchFaceId)

    }

    override suspend fun setFavourite(watchFaceId: Int, isFavourite: String) {
        return watchFaceDao.setFavourite(watchFaceId, isFavourite)
    }

    override suspend fun deleteFavouriteWatchFaces() {
        favouritesWatchFaceDao.deleteAll()
    }


    override suspend fun getFavouriteWatchFaces(isForceRefresh: Boolean): List<WatchFace> {

        if (isForceRefresh) {
            deleteFavouriteWatchFaces()
            return ArrayList()
        } else {
            val watchFaceList = ArrayList<WatchFace>()
            val localData = favouritesWatchFaceDao.getFavouriteWatchFaces()

            localData?.forEach {
                val wf = WatchFace(
                    imageUrl = it.imageUrl,
                    id = it.id,
                    name = it.name,
                    downloads = it.downloads,
                    is_favourite = it.is_favourite
                )
                watchFaceList.add(wf)
            }
            return watchFaceList
        }
    }

    override suspend fun addToFavourites(watchFace: FavouriteWatchFace) {
        favouritesWatchFaceDao.insert(watchFace)
    }

    override suspend fun removeFromFavourites(watchFaceId: Int) {
        favouritesWatchFaceDao.removeWatchFace(watchFaceId)
    }
}
