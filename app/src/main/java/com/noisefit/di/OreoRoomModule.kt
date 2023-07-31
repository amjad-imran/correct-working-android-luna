package com.noisefit.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.noisefit.data.local.db.abstraction.SleepDataSource
import com.noisefit.data.local.db.abstraction.StepsDataSource
import com.noisefit.data.local.db.database.SleepDao
import com.noisefit.data.local.db.database.StepsDao
import com.noisefit.data.local.db.implementation.SleepDataImpl
import com.noisefit.data.local.db.implementation.StepsDataImpl
import com.oreo.data.db.OreoDataBase
import com.oreo.data.db.abstaction.OreoBodyTemperatureDataSource
import com.oreo.data.db.abstaction.OreoDayTimeMovementDataSource
import com.oreo.data.db.abstaction.OreoSleepDataSource
import com.oreo.data.db.abstaction.OreoStepsDataSource
import com.oreo.data.db.database.OreoBloodOxygenDao
import com.oreo.data.db.database.OreoBodyTemperatureDao
import com.oreo.data.db.database.OreoDayTimeMovementDao
import com.oreo.data.db.database.OreoHeartRateDao
import com.oreo.data.db.database.OreoRespiratoryDao
import com.oreo.data.db.database.OreoSleepDao
import com.oreo.data.db.database.OreoStepsDao
import com.oreo.data.db.database.OreoStressDao
import com.oreo.data.db.implementation.OreoBodyTemperatureDataImpl
import com.oreo.data.db.implementation.OreoDayTimeMovementDataImpl
import com.oreo.data.db.implementation.OreoSleepDataImpl
import com.oreo.data.db.implementation.OreoStepsDataImpl
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
            .build()
    }

   /* private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `sleep_data` ADD COLUMN readiness_score INTEGER")
        }
    }

    private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
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
    fun providesStepsDao(database: OreoDataBase): OreoStepsDao {
        return database.stepsDao()
    }

    @Singleton
    @Provides
    fun providesSleepDao(database: OreoDataBase): OreoSleepDao {
        return database.sleepDao()
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
    fun providesBODao(database: OreoDataBase): OreoBloodOxygenDao {
        return database.bloodOxygenDao()
    }

    @Singleton
    @Provides
    fun providesDayTimeDao(database: OreoDataBase): OreoDayTimeMovementDao {
        return database.dayTimeMovementDao()
    }

    @Singleton
    @Provides
    fun provideTempDataImpl(tempDao: OreoBodyTemperatureDao): OreoBodyTemperatureDataImpl {
        return OreoBodyTemperatureDataImpl(tempDao)
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
}