package com.noisefit.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.noisefit.data.local.db.DataBase
import com.noisefit.data.local.db.abstraction.*
import com.noisefit.data.local.db.database.*
import com.noisefit.data.local.db.implementation.*
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext appContext: Context): DataBase {
        return Room.databaseBuilder(appContext, DataBase::class.java, "noisefit-db")
            .addMigrations(
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12,
                MIGRATION_12_13,
                MIGRATION_13_14,
                MIGRATION_14_15,
                MIGRATION_15_16,
                MIGRATION_16_17,
                MIGRATION_17_18,
                MIGRATION_18_19,
                MIGRATION_19_20
            ).build()
    }

    private val MIGRATION_8_9: Migration = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `body_temperature` (`id` INTEGER NOT NULL, `is_synced` INTEGER NOT NULL,`resetData` INTEGER NOT NULL,`value` REAL,`date` TEXT,`time` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_body_temperature_value_date_time ON  body_temperature(value, date, time)")
        }
    }

    private val MIGRATION_9_10: Migration = object : Migration(9, 10) { //9 -> 12
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `activities` ADD COLUMN extreme_min INTEGER")
        }
    }

    private val MIGRATION_19_20: Migration = object : Migration(19, 20) { //9 -> 12
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `activities` ADD COLUMN w_temp REAL")
            database.execSQL("ALTER TABLE `activities` ADD COLUMN uvi REAL")
            database.execSQL("ALTER TABLE `activities` ADD COLUMN humidity REAL")
            database.execSQL("ALTER TABLE `activities` ADD COLUMN start TEXT")
            database.execSQL("ALTER TABLE `activities` ADD COLUMN r_end TEXT")
        }
    }

    private val MIGRATION_14_15: Migration = object : Migration(14, 15) { //14 -> 15
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `activities` ADD COLUMN isHrZoneInSeconds INTEGER")
        }
    }
    private val MIGRATION_15_16: Migration = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS watch_faces")

            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `watch_faces` (`uId` INTEGER NOT NULL," +
                        " `lastSync` INTEGER ," +
                        "`watchfaceCatId` INTEGER," +
                        "`active` INTEGER ," +
                        "`deviceId` INTEGER ," +
                        "`imageUrl` TEXT," +
                        "`fileName` TEXT ," +
                        "`faceId` TEXT ," +
                        "`description` TEXT," +
                        "`displayViews` TEXT ," +
                        "`createdAt` TEXT ," +
                        "`editable` INTEGER ," +
                        "`zipName` TEXT ," +
                        "`displayLikes` TEXT ," +
                        "`updatedAt` TEXT ," +
                        "`faceType` INTEGER ," +
                        "`name` TEXT ," +
                        "`custom` INTEGER ," +
                        "`displayDownloads` TEXT ," +
                        "`downloads` TEXT ," +
                        "`inUseCount` TEXT ," +
                        "`id` INTEGER," +
                        "`imageType` TEXT ," +
                        "`zip_file` TEXT ," +
                        "`is_favourite` TEXT ," +
                        "`fileUrl` TEXT " +
                        ", PRIMARY KEY(`uId`))"
            )

        }
    }

    private val MIGRATION_16_17: Migration = object : Migration(16, 17) {
        override fun migrate(database: SupportSQLiteDatabase) {


            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `favourite_watch_faces` (`uId` INTEGER NOT NULL," +
                        " `lastSync` INTEGER ," +
                        "`imageUrl` TEXT ," +
                        "`description` TEXT ," +
                        "`name` TEXT ," +
                        "`downloads` TEXT ," +
                        "`imageType` TEXT ," +
                        "`id` INTEGER," +
                        "`is_favourite` TEXT, PRIMARY KEY(`uId`))"
            )
        }
    }

    private val MIGRATION_17_18: Migration = object : Migration(17, 18) {
        override fun migrate(database: SupportSQLiteDatabase) {


            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `key_value` (`uId` INTEGER NOT NULL," +
                        " `lastSync` INTEGER ," +
                        "`key` TEXT ," +
                        "`type` TEXT ," +
                        "`value` TEXT , PRIMARY KEY(`uId`))"
            )
        }
    }
    private val MIGRATION_18_19: Migration = object : Migration(18, 19) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `feeds` (`uId` INTEGER NOT NULL," +
                        " `lastSync` INTEGER ," +
                        "`key` TEXT ," +
                        "`type` TEXT ," +
                        "`value` TEXT , PRIMARY KEY(`uId`))"
            )
        }
    }

    private val MIGRATION_10_11: Migration = object : Migration(10, 11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `watch_faces` (`uId` INTEGER NOT NULL, `lastSync` INTEGER ,`watchfaceCatId` INTEGER,`active` INTEGER ,`deviceId` INTEGER ,`imageUrl` TEXT,`fileName` TEXT ,`faceId` TEXT ,`about` TEXT,`displayViews` TEXT ,`createdAt` TEXT ,`editable` INTEGER ,`zipName` TEXT ,`displayLikes` TEXT ,`updatedAt` TEXT ,`faceType` INTEGER ,`name` TEXT ,`custom` INTEGER ,`displayDownloads` TEXT ,`downloads` TEXT ,`id` INTEGER,`imageType` TEXT ,`fileUrl` TEXT , PRIMARY KEY(`uId`))")

        }
    }

    private val MIGRATION_11_12: Migration = object : Migration(11, 12) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("Alter TABLE  `steps_data` ADD COLUMN sync_date INTEGER")

        }
    }

    private val MIGRATION_12_13: Migration = object : Migration(12, 13) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("Alter TABLE `sleep_data` ADD COLUMN sync_date INTEGER")
            database.execSQL("Alter TABLE `heart_rate` ADD COLUMN sync_date INTEGER")
            database.execSQL("Alter TABLE `blood_oxygen` ADD COLUMN sync_date INTEGER")
            database.execSQL("Alter TABLE `body_temperature` ADD COLUMN sync_date INTEGER")
            database.execSQL("Alter TABLE `stress_data` ADD COLUMN sync_date INTEGER")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_activities_date_endTime ON  activities(date, endTime)")
            database.execSQL("ALTER TABLE `activities` ADD COLUMN gps_cord TEXT")

        }
    }

    private val MIGRATION_13_14: Migration = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `sport_event` (`uId` INTEGER NOT NULL, `eventId` TEXT NOT NULL ,`timeInMilliseconds` INTEGER NOT NULL,`date` TEXT NOT NULL ,`time` TEXT NOT NULL ,`title` TEXT  NOT NULL,`venue` TEXT NOT NULL , PRIMARY KEY(`uId`))")


        }
    }

    @Singleton
    @Provides
    fun providesActivityDao(database: DataBase): ActivityDao {
        return database.activityDao()
    }

    @Singleton
    @Provides
    fun providesKeyValueDao(database: DataBase): KeyValueDao {
        return database.keyValueDao()
    }

    @Singleton
    @Provides
    fun providesFeedDbValueDao(database: DataBase): FeedsDao {
        return database.feedDao()
    }

    @Singleton
    @Provides
    fun providesStepsDao(database: DataBase): StepsDao {
        return database.stepsDao()
    }

    @Singleton
    @Provides
    fun providesWatchFaceDao(database: DataBase): WatchFaceDao {
        return database.watchFaceDao()
    }

    @Singleton
    @Provides
    fun providesFavouriteWatchFaceDao(database: DataBase): FavouriteWatchFaceDao {
        return database.favouriteWatchFaceDao()
    }

    @Singleton
    @Provides
    fun providesSportEventDao(database: DataBase): SportEventDao {
        return database.sportEventDao()
    }

    @Singleton
    @Provides
    fun providesBodyTemperatureDao(database: DataBase): BodyTemperatureDao {
        return database.bodyTemperatureDao()
    }

    @Singleton
    @Provides
    fun providesBloodOxygenDao(database: DataBase): BloodOxygenDao {
        return database.bloodOxygenDao()
    }

    @Singleton
    @Provides
    fun providesStressDao(database: DataBase): StressDao {
        return database.stressDao()
    }

    @Singleton
    @Provides
    fun providesHeartRateDao(database: DataBase): HeartRateDao {
        return database.heartDao()
    }

    @Singleton
    @Provides
    fun providesSleepDao(database: DataBase): SleepDao {
        return database.sleepDao()
    }

    @Singleton
    @Provides
    fun providesGoogleFitDao(database: DataBase): GoogleFitDao {
        return database.googleFitDao()
    }

    @Singleton
    @Provides
    fun providesBloodPressureDao(database: DataBase): BloodPressureDao {
        return database.bloodPressureDao()
    }

    @Singleton
    @Provides
    fun provideStepsDataImpl(stepsDao: StepsDao): StepsDataImpl {
        return StepsDataImpl(stepsDao)
    }

    @Singleton
    @Provides
    fun provideBodyTemperatureDao(bodyTemperatureDao: BodyTemperatureDao): BodyTemperatureDataImpl {
        return BodyTemperatureDataImpl(bodyTemperatureDao)
    }

    @Singleton
    @Provides
    fun provideWatchFaceDao(
        watchFaceDao: WatchFaceDao,
        favouriteWatchFaceDao: FavouriteWatchFaceDao,
        dataStoredInterface: DataStoredInterface
    ): WatchFaceDataSourceImpl {
        return WatchFaceDataSourceImpl(watchFaceDao, dataStoredInterface, favouriteWatchFaceDao)
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
    fun provideFeedsDataSource(
        keyValueDao: FeedsDao,
    ): FeedsDataSource {
        return FeedsDataSourceImpl(keyValueDao)
    }

    @Singleton
    @Provides
    fun provideSportEventDao(sportEventDao: SportEventDao): SportEventDataImpl {
        return SportEventDataImpl(sportEventDao)
    }

    @Singleton
    @Provides
    fun provideStepsDataInterface(stepsDataImpl: StepsDataImpl): StepsDataSource {
        return stepsDataImpl
    }

    @Singleton
    @Provides
    fun provideHeartRateDataImpl(heartRateDao: HeartRateDao): HeartRateDataImpl {
        return HeartRateDataImpl(heartRateDao)
    }

    @Singleton
    @Provides
    fun provideHeartRateInterface(heartRateDataImpl: HeartRateDataImpl): HeartRateDataSource {
        return heartRateDataImpl
    }

    @Singleton
    @Provides
    fun provideSleepImpl(sleepDao: SleepDao): SleepDataImpl {
        return SleepDataImpl(sleepDao)
    }

    @Singleton
    @Provides
    fun provideGoogleFitDataImpl(
        googleFitDao: GoogleFitDao,
        stepsDao: StepsDao,
        heartRateDao: HeartRateDao,
        sleepDao: SleepDao
    ): GoogleFitDataImpl {
        return GoogleFitDataImpl(googleFitDao, stepsDao, heartRateDao, sleepDao)
    }

    @Singleton
    @Provides
    fun provideSleepInterface(sleepDataImpl: SleepDataImpl): SleepDataSource {
        return sleepDataImpl
    }

    @Singleton
    @Provides
    fun provideBodyTemperatureDataImpl(bodyTemperatureDataImpl: BodyTemperatureDataImpl): BodyTemperatureDataSource {
        return bodyTemperatureDataImpl
    }

    @Singleton
    @Provides
    fun provideBloodPressureDataImpl(data: BloodPressureDao): BloodPressureDataImpl {
        return BloodPressureDataImpl(data)
    }

    @Singleton
    @Provides
    fun provideBloodPressureInterface(data: BloodPressureDataImpl): BloodPressureDataSource {
        return data
    }

    @Singleton
    @Provides
    fun provideBloodOxygenDataImpl(data: BloodOxygenDao): BloodOxygenDataImpl {
        return BloodOxygenDataImpl(data)
    }

    @Singleton
    @Provides
    fun provideBloodOxygenInterface(data: BloodOxygenDataImpl): BloodOxygenDataSource {
        return data
    }

    @Singleton
    @Provides
    fun provideStressDataImpl(data: StressDao): StressDataImpl {
        return StressDataImpl(data)
    }

    @Singleton
    @Provides
    fun provideStressDataInterface(data: StressDataImpl): StressDataSource {
        return data
    }
}