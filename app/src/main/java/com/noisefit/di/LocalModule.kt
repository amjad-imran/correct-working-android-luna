package com.noisefit.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.noisefit.data.local.DataConstants.DATASTORED_NAME
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.local.dataStored.abstraction.ILastSyncStore
import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.noisefit.data.local.dataStored.implementation.DataStoredImpl
import com.noisefit.data.local.dataStored.implementation.LastSyncStoreImpl
import com.noisefit.data.local.dataStored.implementation.OfflineApiResponseStoreImpl
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit_commans.data.local.abstraction.DataStoredInterface


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {


    @Singleton
    @Provides
    fun provideSharedPreferences(
        @ApplicationContext appContext: Context,
        @Named("DataStoredName") name: String
    ): SharedPreferences {
        return appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    @Named("DataStoredName")
    @Singleton
    @Provides
    fun provideSharedPrefName(): String {
        return DATASTORED_NAME
    }


    @Singleton
    @Provides
    fun provideDataStoredImpl(gson: Gson, sharedPreferences: SharedPreferences): DataStoredImpl {
        return DataStoredImpl(gson, sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideLastSyncStore(sharedPreferences: SharedPreferences): ILastSyncStore {
        return LastSyncStoreImpl(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideOfflineApiStore(
        gson: Gson,
        sharedPreferences: SharedPreferences
    ): IOfflineApiResponseStore {
        return OfflineApiResponseStoreImpl(gson, sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideDataStoreInterface(dataStoredImpl: DataStoredImpl): DataStoredInterface {
        return dataStoredImpl
    }

    @Singleton
    @Provides
    fun provideLastSyncProvider(lastSyncStore: ILastSyncStore): LastSyncProvider {
        return LastSyncProvider(lastSyncStore)
    }


    @Singleton
    @Provides
    fun provideResourceProvider(@ApplicationContext appContext: Context): ResourcesProvider {
        return ResourcesProvider(appContext)
    }
}