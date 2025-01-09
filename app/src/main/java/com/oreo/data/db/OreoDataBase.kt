package com.oreo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.noisefit.data.local.db.Converters
import com.noisefit.data.local.db.database.KeyValueDao
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.GoogleFitDataDb
import com.noisefit_commans.data.model.GoogleFitWorkoutData
import com.noisefit_commans.data.model.KeyValue
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBloodPressureData
import com.noisefit_commans.data.model.OreoBodyStressData
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.data.model.OreoGoogleFitData
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.noisefit_commans.data.model.RecordedWorkoutData
import com.noisefit_commans.data.model.UserHealthData
import com.oreo.data.db.database.OreoAutoSportDao
import com.oreo.data.db.database.OreoBloodOxygenDao
import com.oreo.data.db.database.OreoBodyStressDao
import com.oreo.data.db.database.OreoBodyTemperatureDao
import com.oreo.data.db.database.OreoDayTimeMovementDao
import com.oreo.data.db.database.OreoGFitDataDao
import com.oreo.data.db.database.OreoGFitWorkoutDao
import com.oreo.data.db.database.OreoHeartRateDao
import com.oreo.data.db.database.OreoNapDao
import com.oreo.data.db.database.OreoRecordedWorkoutDao
import com.oreo.data.db.database.OreoRespiratoryDao
import com.oreo.data.db.database.OreoSleepDao
import com.oreo.data.db.database.OreoStepsDao
import com.oreo.data.db.database.OreoStressDao
import com.oreo.data.db.database.OreoUserHealthDataDao

@Database(
    entities = [OreoStepsData::class, OreoHeartRate::class, OreoBloodOxygenBreakup::class,
        OreoBloodPressureData::class, OreoSleepData::class, OreoStressDataBreakup::class, OreoGoogleFitData::class,
        OreoBodyTemperatureBreakup::class, OreoRespiratoryData::class, DayTimeMovementBreakup::class,
        OreoAutoSportData::class, RecordedWorkoutData::class, KeyValue::class, UserHealthData::class, OreoNapData::class,
        GoogleFitWorkoutData::class, OreoBodyStressData::class, GoogleFitDataDb::class],
    version = 11, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OreoDataBase : RoomDatabase() {
    //abstract fun sportEventDao(): SportEventDao
    abstract fun stepsDao(): OreoStepsDao
    abstract fun oreoAutoSportDao(): OreoAutoSportDao
    abstract fun oreoRecordedWorkoutDap(): OreoRecordedWorkoutDao
    abstract fun heartDao(): OreoHeartRateDao
    abstract fun stressDao(): OreoStressDao
    abstract fun bodyTemperatureDao(): OreoBodyTemperatureDao
    abstract fun bloodOxygenDao(): OreoBloodOxygenDao
    abstract fun sleepDao(): OreoSleepDao
    abstract fun napDao(): OreoNapDao
    abstract fun respiratoryDao(): OreoRespiratoryDao
    abstract fun bodyStressDao(): OreoBodyStressDao
    abstract fun dayTimeMovementDao(): OreoDayTimeMovementDao
    abstract fun userHealthDataDao(): OreoUserHealthDataDao

    @Deprecated("use gFitDataDao")
    abstract fun gFitWorkoutDao(): OreoGFitWorkoutDao

    abstract fun gFitDataDao(): OreoGFitDataDao

    abstract fun keyValueDao(): KeyValueDao

    /*abstract fun googleFitDao(): GoogleFitDao*/

}