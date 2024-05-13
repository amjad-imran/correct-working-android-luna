package com.noisefit_commans.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location", indices = [Index(value = ["timestamp"], unique = true)]
)
data class LocationModel(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "lat") var lat: Double? = null,
    @ColumnInfo(name = "longitude") var longitude: Double? = null,
    @ColumnInfo(name = "altitude") var altitude: Double? = null,
    @ColumnInfo(name = "is_running") var isRunning: Boolean = true,
    @ColumnInfo(name = "timestamp") var timeStamp: Long = 0,
    @ColumnInfo(name = "temperature") var temperature: Double? = null,
    @ColumnInfo(name = "location") var location: String? = null,
    @ColumnInfo(name = "weather_status") var weatherStatus: Int? = null,

    )