package com.noisefit_commans.data.db.abstraction

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.noisefit_commans.data.db.LocationModel

@Dao
interface LocationDao {

    @Query("SELECT * FROM location where timestamp>=:startTimeStamp AND timestamp<=:endTimeStamp")
    fun getLocationData(startTimeStamp: Long, endTimeStamp: Long): List<LocationModel>


    @Query("DELETE FROM location")
    fun deleteAllData()

    @Insert
    fun insertData(data: LocationModel)


    @Query("UPDATE location SET temperature = :temp, weather_status = :status WHERE lat =:lat AND longitude =:lng")
    fun updateWeatherInfoForLatLong(lat: Double, lng: Double, temp: Double, status: Int?)
}