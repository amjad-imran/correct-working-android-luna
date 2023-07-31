package com.noisefit_commans.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.local.implementation.WatchDataStoreImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WatchModule {

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }


    @Named("WatchDataStoreSharedPref")
    @Singleton
    @Provides
    fun provideSharedPreferences(
        @ApplicationContext appContext: Context,
        @Named("WatchDataStore") name: String
    ): SharedPreferences {
        return appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    @Named("WatchDataStore")
    @Singleton
    @Provides
    fun provideSharedPrefName(): String {
        return "noise_fit_watch"
    }


    @Singleton
    @Provides
    fun provideWatchDataStoreImpl(
        gson: Gson, @Named("WatchDataStoreSharedPref")
        sharedPreferences: SharedPreferences
    ): WatchDataStoreImpl {
        return WatchDataStoreImpl(gson, sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideWatchDataStoreInterface(watchDataStore: WatchDataStoreImpl): WatchDataStore {
        return watchDataStore
    }

}