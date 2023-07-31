package com.noisefit.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.noisefit.data.local.db.database.*
import com.noisefit.data.model.FeedsDbValue
import com.noisefit_commans.data.model.KeyValue
import com.noisefit_commans.data.model.matches.SportEvent
import com.noisefit_commans.models.*

@Database(
    entities = [SportsModeResponse::class, StepsData::class, HeartRate::class, BloodOxygenBreakup::class,
        BloodPressureData::class, SleepData::class, StressDataBreakup::class, GoogleFitData::class,
        BodyTemperatureBreakup::class, WatchFace::class, SportEvent::class, FavouriteWatchFace::class, KeyValue::class,
        FeedsDbValue::class],
    version = 20
)
@TypeConverters(Converters::class)
abstract class DataBase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun sportEventDao(): SportEventDao
    abstract fun stepsDao(): StepsDao
    abstract fun watchFaceDao(): WatchFaceDao
    abstract fun favouriteWatchFaceDao(): FavouriteWatchFaceDao
    abstract fun heartDao(): HeartRateDao
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun bloodOxygenDao(): BloodOxygenDao
    abstract fun sleepDao(): SleepDao
    abstract fun googleFitDao(): GoogleFitDao
    abstract fun stressDao(): StressDao
    abstract fun bodyTemperatureDao(): BodyTemperatureDao
    abstract fun keyValueDao(): KeyValueDao
    abstract fun feedDao(): FeedsDao

}