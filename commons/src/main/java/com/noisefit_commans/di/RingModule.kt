package com.noisefit_commans.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.implementation.RingDataStoreImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RingModule {


    @Named("RingDataStoreSharedPref")
    @Singleton
    @Provides
    fun provideSharedPreferences(
        @ApplicationContext appContext: Context,
        @Named("RingDataStore") name: String
    ): SharedPreferences {
        return appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
    @Named("RingDataStore")
    @Singleton
    @Provides
    fun provideSharedPrefName(): String {
        return "noise_fit_ring"
    }

    @Singleton
    @Provides
    fun provideRingDataStoreImpl(
        gson: Gson,
        @Named("RingDataStoreSharedPref")
        sharedPreferences: SharedPreferences
    ): RingDataStoreImpl {
        return RingDataStoreImpl(gson, sharedPreferences)
    }
    @Singleton
    @Provides
    fun provideRingDataStoreInterface(ringDataStore: RingDataStoreImpl): RingDataStore {
        return ringDataStore
    }
}