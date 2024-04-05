package com.noisefit.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.database.KeyValueDao
import com.noisefit.data.local.db.implementation.KeyValueDataSourceImpl
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.OreoDataBase
import com.oreo.data.db.abstaction.OreoBodyTemperatureDataSource
import com.oreo.data.db.abstaction.OreoDayTimeMovementDataSource
import com.oreo.data.db.abstaction.OreoNapDataSource
import com.oreo.data.db.abstaction.OreoSleepDataSource
import com.oreo.data.db.abstaction.OreoStepsDataSource
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.db.database.OreoAutoSportDao
import com.oreo.data.db.database.OreoBloodOxygenDao
import com.oreo.data.db.database.OreoBodyStressDao
import com.oreo.data.db.database.OreoBodyTemperatureDao
import com.oreo.data.db.database.OreoDayTimeMovementDao
import com.oreo.data.db.database.OreoGFitWorkoutDao
import com.oreo.data.db.database.OreoHeartRateDao
import com.oreo.data.db.database.OreoNapDao
import com.oreo.data.db.database.OreoRecordedWorkoutDao
import com.oreo.data.db.database.OreoRespiratoryDao
import com.oreo.data.db.database.OreoSleepDao
import com.oreo.data.db.database.OreoStepsDao
import com.oreo.data.db.database.OreoStressDao
import com.oreo.data.db.database.OreoUserHealthDataDao
import com.oreo.data.db.implementation.OreoBodyStressDataImpl
import com.oreo.data.db.implementation.OreoBodyTemperatureDataImpl
import com.oreo.data.db.implementation.OreoDayTimeMovementDataImpl
import com.oreo.data.db.implementation.OreoNapDataImpl
import com.oreo.data.db.implementation.OreoGFitWorkoutDataImpl
import com.oreo.data.db.implementation.OreoSleepDataImpl
import com.oreo.data.db.implementation.OreoStepsDataImpl
import com.oreo.data.db.implementation.OreoUserHealthDataDataImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class OreoRoomModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext appContext: Context): OreoDataBase {
        return Room.databaseBuilder(appContext, OreoDataBase::class.java, "noisefit-db-oreo")
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_4)
            .addMigrations(MIGRATION_4_5)
            .addMigrations(MIGRATION_5_6)
            .addMigrations(MIGRATION_6_7)
            .addMigrations(MIGRATION_7_8)
            .addMigrations(MIGRATION_8_9)
            .build()
    }

    private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `key_value` " +
                        "(`uId` INTEGER NOT NULL, " +
                        "`lastSync` INTEGER," +
                        "`value` TEXT," +
                        "`type` TEXT," +
                        "`key` TEXT, PRIMARY KEY(`uId`))"
            )
        }
    }

    private val MIGRATION_7_8: Migration = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("Alter TABLE `recorded_workout` ADD COLUMN cadence INTEGER")
            database.execSQL("Alter TABLE `recorded_workout` ADD COLUMN distance INTEGER")
            database.execSQL("Alter TABLE `recorded_workout` ADD COLUMN recovery_time INTEGER")
        }
    }


    private val MIGRATION_2_4: Migration = object : Migration(2, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `user_health_data` " +
                        "(`id` INTEGER NOT NULL, " +
                        "`trendData` TEXT," +
                        "`userHealthData` TEXT," +
                        "`date` TEXT, PRIMARY KEY(`id`))"
            )
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_user_health_data_date ON  user_health_data(date)")

        }
    }

    private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `recorded_workout` " +
                        "(`id` INTEGER NOT NULL, " +
                        "`is_synced` INTEGER NOT NULL," +
                        "`is_accepted` INTEGER NOT NULL," +
                        "`duration` INTEGER," +
                        "`intensity` INTEGER," +
                        "`calories` INTEGER," +
                        "`startTime` INTEGER NOT NULL," +
                        "`endTime` INTEGER NOT NULL," +
                        "`steps` INTEGER," +
                        "`type` INTEGER," +
                        "`hr` TEXT," +
                        "`intensity_list` TEXT," +
                        "`date` TEXT, PRIMARY KEY(`id`))"
            )
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_recorded_workout_startTime ON  recorded_workout(startTime)")

        }
    }

    private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `nap_data` " +
                        "(`id` INTEGER NOT NULL, " +
                        "`is_synced` INTEGER NOT NULL," +
                        "`is_google_fit_sync` INTEGER NOT NULL," +
                        "`start_time` TEXT," +
                        "`end_time` TEXT," +
                        "`duration` INTEGER NOT NULL," +
                        "`date` TEXT, PRIMARY KEY(`id`))"
            )
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_nap_data_start_time_end_time ON  nap_data(start_time,end_time)")

        }
    }

    private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `google_fit_workout` " +
                        "(`id` INTEGER NOT NULL, " +
                        "`is_synced` INTEGER NOT NULL," +
                        "`name` TEXT," +
                        "`identifier` TEXT," +
                        "`appPackageName` TEXT," +
                        "`activity` TEXT," +
                        "`startTime` INTEGER," +
                        "`endTime` INTEGER," +
                        "`distance` REAL," +
                        "`duration` INTEGER," +
                        "`calories` REAL," +
                        "`heartRate` INTEGER," +
                        "`steps` INTEGER," +
                        "`type` TEXT, PRIMARY KEY(`id`))"
            )
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_google_fit_workout_startTime ON  google_fit_workout(startTime)")

        }
    }

    private val MIGRATION_8_9: Migration = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `body_stress` " +
                        "(`id` INTEGER NOT NULL, " +
                        "`is_synced` INTEGER NOT NULL," +
                        "`is_google_fit_sync` INTEGER NOT NULL," +
                        "`break_up` TEXT," +
                        "`date` TEXT, PRIMARY KEY(`id`))"
            )
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_body_stress_date ON  body_stress(date)")

        }
    }


    /*private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `day_time_movement` " +
                        "(`id` INTEGER NOT NULL, `is_synced` INTEGER NOT NULL,`break_up` TEXT,`is_google_fit_sync` INTEGER  NOT NULL,`date` TEXT, PRIMARY KEY(`id`))"
            )
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_day_time_movement_date ON  day_time_movement(date)")
        }
    }

    private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `steps_data` ADD COLUMN active_calories INTEGER")

        }
    }*/


    @Singleton
    @Provides
    fun provideUserHealthDataSource(
        userHealthDao: OreoUserHealthDataDao
    ): OreoUserHealthDataDataSource {
        return OreoUserHealthDataDataImpl(userHealthDao)
    }

    @Singleton
    @Provides
    fun provideKeyValueDataSource(
        keyValueDao: KeyValueDao,
    ): KeyValueDataSource {
        return KeyValueDataSourceImpl(keyValueDao)
    }


    @Singleton
    @Provides
    fun providesStepsDao(database: OreoDataBase): OreoStepsDao {
        return database.stepsDao()
    }

    @Singleton
    @Provides
    fun providesOreoGFitWorkoutDao(database: OreoDataBase): OreoGFitWorkoutDao {
        return database.gFitWorkoutDao()
    }

    @Singleton
    @Provides
    fun providesSleepDao(database: OreoDataBase): OreoSleepDao {
        return database.sleepDao()
    }

    @Singleton
    @Provides
    fun providesNapDao(database: OreoDataBase): OreoNapDao {
        return database.napDao()
    }

    @Singleton
    @Provides
    fun providesRespiratoryDao(database: OreoDataBase): OreoRespiratoryDao {
        return database.respiratoryDao()
    }

    @Singleton
    @Provides
    fun providesStressDao(database: OreoDataBase): OreoStressDao {
        return database.stressDao()
    }

    @Singleton
    @Provides
    fun providesHrDao(database: OreoDataBase): OreoHeartRateDao {
        return database.heartDao()
    }


    @Singleton
    @Provides
    fun provideStepsDataImpl(stepsDao: OreoStepsDao): OreoStepsDataImpl {
        return OreoStepsDataImpl(stepsDao)
    }


    @Singleton
    @Provides
    fun provideOreoGFitWorkoutDataImpl(data: OreoGFitWorkoutDao): OreoGFitWorkoutDataImpl {
        return OreoGFitWorkoutDataImpl(data)
    }


    @Singleton
    @Provides
    fun provideStepsData(stepsDataImpl: OreoStepsDataImpl): OreoStepsDataSource {
        return stepsDataImpl
    }

    @Singleton
    @Provides
    fun providesTempDao(database: OreoDataBase): OreoBodyTemperatureDao {
        return database.bodyTemperatureDao()
    }

    @Singleton
    @Provides
    fun providesBodyStressDao(database: OreoDataBase): OreoBodyStressDao {
        return database.bodyStressDao()
    }

    @Singleton
    @Provides
    fun providesBODao(database: OreoDataBase): OreoBloodOxygenDao {
        return database.bloodOxygenDao()
    }

    @Singleton
    @Provides
    fun providesAutoSportDao(database: OreoDataBase): OreoAutoSportDao {
        return database.oreoAutoSportDao()
    }

    @Singleton
    @Provides
    fun providesRecordedWorkoutDao(database: OreoDataBase): OreoRecordedWorkoutDao {
        return database.oreoRecordedWorkoutDap()
    }

    @Singleton
    @Provides
    fun providesUserHealthDataDao(database: OreoDataBase): OreoUserHealthDataDao {
        return database.userHealthDataDao()
    }

    @Singleton
    @Provides
    fun providesUserHealthDataImpl(userHealthDataDao: OreoUserHealthDataDao): OreoUserHealthDataDataImpl {
        return OreoUserHealthDataDataImpl(userHealthDataDao)
    }

    @Singleton
    @Provides
    fun providesDayTimeDao(database: OreoDataBase): OreoDayTimeMovementDao {
        return database.dayTimeMovementDao()
    }

    @Singleton
    @Provides
    fun providesKeyValueDao(database: OreoDataBase): KeyValueDao {
        return database.keyValueDao()
    }

    @Singleton
    @Provides
    fun provideTempDataImpl(tempDao: OreoBodyTemperatureDao): OreoBodyTemperatureDataImpl {
        return OreoBodyTemperatureDataImpl(tempDao)
    }

    @Singleton
    @Provides
    fun provideBodyStressDataImpl(bodyStressDao: OreoBodyStressDao): OreoBodyStressDataImpl {
        return OreoBodyStressDataImpl(bodyStressDao)
    }

    @Singleton
    @Provides
    fun provideDayTimeMovementImpl(dayTimeDao: OreoDayTimeMovementDao): OreoDayTimeMovementDataImpl {
        return OreoDayTimeMovementDataImpl(dayTimeDao)
    }


    @Singleton
    @Provides
    fun provideDayTimeData(dayTime: OreoDayTimeMovementDataImpl): OreoDayTimeMovementDataSource {
        return dayTime
    }

    @Singleton
    @Provides
    fun provideTemperatureData(tempData: OreoBodyTemperatureDataImpl): OreoBodyTemperatureDataSource {
        return tempData
    }

    @Singleton
    @Provides
    fun provideSleepData(sleepDao: OreoSleepDao): OreoSleepDataSource {
        return OreoSleepDataImpl(sleepDao)
    }

    @Singleton
    @Provides
    fun provideNapData(napDao: OreoNapDao): OreoNapDataSource {
        return OreoNapDataImpl(napDao)
    }

}