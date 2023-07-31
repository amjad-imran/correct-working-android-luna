package com.noisefit_evolve2.di

import android.content.Context
import com.google.gson.Gson
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_evolve2.base.Evolve2ApplicationHandler
import com.noisefit_evolve2.handler.Evolve2QueryDeviceUnitsHandler
import com.noisefit_evolve2.handler.Evolve2UpdateDeviceUnitsHandler
import com.noisefit_evolve2.handler.Evolve2UserActivityHandler
import com.noisefit_evolve2.handler.connect.Evolve2ConnectHandler
import com.noisefit_evolve2.dataConversion.DataConverter
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
    fun provideDataConverter(
        @ApplicationContext appContext: Context,
        gson: Gson
    ): DataConverter {
        return DataConverter(appContext, gson)
    }


    @Singleton
    @Provides
    fun provideEvolve2UpdateDeviceUnitsHandler(
        dataConverter: DataConverter,
        @ApplicationContext appContext: Context,
        evolve2ApplicationHandler: Evolve2ApplicationHandler,
        watchDataStore: WatchDataStore
    ): Evolve2UpdateDeviceUnitsHandler {
        return Evolve2UpdateDeviceUnitsHandler(
            dataConverter,
            appContext,
            evolve2ApplicationHandler,
            watchDataStore
        )
    }

    @Singleton
    @Provides
    fun provideEvolve2ApplicationHandler(@ApplicationContext context: Context): Evolve2ApplicationHandler {
        return Evolve2ApplicationHandler(context)
    }

    @Singleton
    @Provides
    fun provideEvolve2QueryDeviceUnitsHandler(
        evolve2ApplicationHandler: Evolve2ApplicationHandler,
        dataConverter: DataConverter,
        @ApplicationContext context: Context,
        watchDataStore: WatchDataStore
    ): Evolve2QueryDeviceUnitsHandler {
        return Evolve2QueryDeviceUnitsHandler(
            evolve2ApplicationHandler,
            dataConverter,
            context,
            watchDataStore
        )
    }

    @Singleton
    @Provides
    fun provideEvolve2UserActivityHandler(
        dataConverter: DataConverter,
        evolveApplicationHandler: Evolve2ApplicationHandler
    ): Evolve2UserActivityHandler {
        return Evolve2UserActivityHandler(dataConverter, evolveApplicationHandler)
    }

    @Singleton
    @Provides
    fun provideEvolve2ConnectHandler(evolve2ApplicationHandler: Evolve2ApplicationHandler): Evolve2ConnectHandler {
        return Evolve2ConnectHandler(evolve2ApplicationHandler)
    }

    @Named("EvolveUpdateDevice")
    @Singleton
    @Provides
    fun provideEvolveUpdateDeviceDataActions(
        evolveUpdateDeviceUnitsHandler: Evolve2UpdateDeviceUnitsHandler
    ): UpdateDeviceDataActions {
        return evolveUpdateDeviceUnitsHandler
    }

    @Named("evolveConnectionDataActions")
    @Singleton
    @Provides
    fun provideConnectionDataActions(evolve2ConnectHandler: Evolve2ConnectHandler): ConnectionDataActions {
        return evolve2ConnectHandler
    }

    @Named("evolveApplicationHandler")
    @Singleton
    @Provides
    fun provideBaseInitializeInterface(evolve2ApplicationHandler: Evolve2ApplicationHandler): BaseInitializeInterface {
        return evolve2ApplicationHandler
    }

    @Named("EvolveUserActivityDataActions")
    @Singleton
    @Provides
    fun provideEvolveUserActivityDataActions(evolve2UserActivityHandler: Evolve2UserActivityHandler): UserActivityDataActions {
        return evolve2UserActivityHandler
    }

    @Named("EvolveQueryDeviceDataActions")
    @Singleton
    @Provides
    fun provideEvolveQueryDeviceDataActions(evolve2QueryDeviceUnitsHandler: Evolve2QueryDeviceUnitsHandler): QueryDeviceDataActions {
        return evolve2QueryDeviceUnitsHandler
    }

}