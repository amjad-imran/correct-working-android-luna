package com.noisefit_commans.di

import android.content.Context
import androidx.room.Room
import com.noisefit_commans.data.db.abstraction.LocationDao
import com.noisefit_commans.data.db.SharedDatabase
import com.noisefit_commans.data.db.abstraction.LocationDataSource
import com.noisefit_commans.data.db.implementation.LocationDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SharedRoomModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext appContext: Context): SharedDatabase {
        return Room.databaseBuilder(appContext, SharedDatabase::class.java, "luna-shared")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideLocationDao(database: SharedDatabase): LocationDao {
        return database.locationDao()
    }

    @Singleton
    @Provides
    fun provideLocationDataSource(
        locationDao: LocationDao,
    ): LocationDataSource {
        return LocationDataSourceImpl(locationDao)
    }

}