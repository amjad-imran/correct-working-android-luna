package com.oreo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.noisefit.data.local.db.Converters
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBloodPressureData
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.data.model.OreoGoogleFitData
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.oreo.data.db.database.OreoBloodOxygenDao
import com.oreo.data.db.database.OreoBodyTemperatureDao
import com.oreo.data.db.database.OreoDayTimeMovementDao
import com.oreo.data.db.database.OreoHeartRateDao
import com.oreo.data.db.database.OreoRespiratoryDao
import com.oreo.data.db.database.OreoSleepDao
import com.oreo.data.db.database.OreoStepsDao
import com.oreo.data.db.database.OreoStressDao

@Database(
    entities = [OreoStepsData::class, OreoHeartRate::class, OreoBloodOxygenBreakup::class,
        OreoBloodPressureData::class, OreoSleepData::class, OreoStressDataBreakup::class, OreoGoogleFitData::class,
        OreoBodyTemperatureBreakup::class, OreoRespiratoryData::class, DayTimeMovementBreakup::class],
    version = 1, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OreoDataBase : RoomDatabase() {
    //abstract fun sportEventDao(): SportEventDao
    abstract fun stepsDao(): OreoStepsDao

    abstract fun heartDao(): OreoHeartRateDao
    abstract fun stressDao(): OreoStressDao
    abstract fun bodyTemperatureDao(): OreoBodyTemperatureDao
    abstract fun bloodOxygenDao(): OreoBloodOxygenDao
    abstract fun sleepDao(): OreoSleepDao
    abstract fun respiratoryDao(): OreoRespiratoryDao
    abstract fun dayTimeMovementDao(): OreoDayTimeMovementDao
    /*abstract fun googleFitDao(): GoogleFitDao*/

}