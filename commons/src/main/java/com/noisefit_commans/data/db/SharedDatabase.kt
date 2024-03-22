package com.noisefit_commans.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.noisefit_commans.data.db.abstraction.LocationDao

@Database(entities = [LocationModel::class], version = 2, exportSchema = false)
abstract class SharedDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao

}