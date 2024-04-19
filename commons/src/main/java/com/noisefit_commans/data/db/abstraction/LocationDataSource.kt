package com.noisefit_commans.data.db.abstraction

import com.noisefit_commans.data.db.LocationModel
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.model.OreoSleepData
import java.util.concurrent.Flow

interface LocationDataSource {

    suspend fun insertData(
        data: LocationModel
    ): Boolean

    suspend fun getLocations(startTimeStamp: Long, endTimeStamp: Long): List<LocationModel>

    suspend fun deleteAll()

    suspend fun updateWeatherInfoForLatLong(lat: Double, lng: Double, temp: Double, status: Int?)

}