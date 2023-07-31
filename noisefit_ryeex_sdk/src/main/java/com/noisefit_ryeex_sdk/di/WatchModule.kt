package com.noisefit_ryeex_sdk.di

import android.content.Context
import com.google.gson.Gson
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler
import com.noisefit_ryeex_sdk.dataConversion.DataConverter
import com.noisefit_ryeex_sdk.handler.RyeexQueryDeviceUnitsHandler
import com.noisefit_ryeex_sdk.handler.RyeexUpdateDeviceUnitsHandler
import com.noisefit_ryeex_sdk.handler.RyeexUserActivityHandler
import com.noisefit_ryeex_sdk.handler.connect.RyeexConnectHandler
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
        gson: Gson,
        watchDataStore: WatchDataStore
    ): DataConverter {
        return DataConverter(appContext,watchDataStore, gson)
    }

    @Singleton
    @Provides
    fun provideRyeexUpdateDeviceUnitsHandler(
        dataConverter: DataConverter,
        @ApplicationContext appContext: Context,
        ryeexApplicationHandler: RyeexApplicationHandler,
        watchDataStore: WatchDataStore
    ): RyeexUpdateDeviceUnitsHandler {
        return RyeexUpdateDeviceUnitsHandler(
            dataConverter,
            appContext,
            ryeexApplicationHandler,
            watchDataStore
        )
    }

    @Singleton
    @Provides
    fun provideRyeexApplicationHandler(@ApplicationContext context: Context): RyeexApplicationHandler {
        return RyeexApplicationHandler(context)
    }

    @Singleton
    @Provides
    fun provideRyeexQueryDeviceUnitsHandler(
        ryeexApplicationHandler: RyeexApplicationHandler,
        dataConverter: DataConverter,
        @ApplicationContext context: Context,
        watchDataStore: WatchDataStore
    ): RyeexQueryDeviceUnitsHandler {
        return RyeexQueryDeviceUnitsHandler(
            ryeexApplicationHandler,
            dataConverter,
            context,
            watchDataStore
        )
    }

    @Singleton
    @Provides
    fun provideRyeexUserActivityHandler(
        dataConverter: DataConverter,
        ryeexApplicationHandler: RyeexApplicationHandler
    ): RyeexUserActivityHandler {
        return RyeexUserActivityHandler(dataConverter, ryeexApplicationHandler)
    }

    @Singleton
    @Provides
    fun provideRyeeexConnectHandler(
        ryeexApplicationHandler: RyeexApplicationHandler,
        watchDataStore: WatchDataStore
    ): RyeexConnectHandler {
        return RyeexConnectHandler(
            ryeexApplicationHandler,
            watchDataStore
        )
    }

    @Named("RyeexUpdateDevice")
    @Singleton
    @Provides
    fun provideRyeexUpdateDeviceDataActions(
        ryeexUpdateDeviceUnitsHandler: RyeexUpdateDeviceUnitsHandler
    ): UpdateDeviceDataActions {
        return ryeexUpdateDeviceUnitsHandler
    }

    @Named("ryeexConnectionDataActions")
    @Singleton
    @Provides
    fun provideConnectionDataActions(ryeexConnectHandler: RyeexConnectHandler): ConnectionDataActions {
        return ryeexConnectHandler
    }

    @Named("ryeexApplicationHandler")
    @Singleton
    @Provides
    fun provideBaseInitializeInterface(ryeexApplicationHandler: RyeexApplicationHandler): BaseInitializeInterface {
        return ryeexApplicationHandler
    }

    @Named("RyeexUserActivityDataActions")
    @Singleton
    @Provides
    fun provideRyeexUserActivityDataActions(ryeexUserActivityHandler: RyeexUserActivityHandler): UserActivityDataActions {
        return ryeexUserActivityHandler
    }

    @Named("RyeexQueryDeviceDataActions")
    @Singleton
    @Provides
    fun provideRyeexQueryDeviceDataActions(ryeexQueryDeviceUnitsHandler: RyeexQueryDeviceUnitsHandler): QueryDeviceDataActions {
        return ryeexQueryDeviceUnitsHandler
    }


}