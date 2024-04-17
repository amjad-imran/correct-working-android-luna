package com.noisefit_commans.data.db.implementation

import android.util.Log
import com.noisefit_commans.data.db.abstraction.LocationDao
import com.noisefit_commans.data.db.LocationModel
import com.noisefit_commans.data.db.abstraction.LocationDataSource
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject

class LocationDataSourceImpl
@Inject
constructor(
    private val locationDao: LocationDao
) : LocationDataSource {

    override suspend fun insertData(data: LocationModel): Boolean {
        locationDao.insertData(data)
        return true
    }

    override suspend fun getLocations(
        startTimeStamp: Long,
        endTimeStamp: Long
    ): List<LocationModel> {
        return locationDao.getLocationData(startTimeStamp, endTimeStamp)
    }

    override suspend fun deleteAll() {
        locationDao.deleteAllData()
    }

    override suspend fun updateWeatherInfoForLatLong(
        lat: Double,
        lng: Double,
        temp: Double,
        status: Int
    ) {
        locationDao.updateWeatherInfoForLatLong(lat, lng, temp, status)
    }
}
