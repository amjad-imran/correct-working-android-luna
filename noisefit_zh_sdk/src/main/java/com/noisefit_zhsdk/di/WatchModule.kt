package com.noisefit.zhsdk.di

import android.content.Context
import android.location.Geocoder
import com.google.gson.Gson
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_zhsdk.base.ZhApplicationHandler
import com.noisefit_zhsdk.handler.*
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
        watchDataStore: WatchDataStore,
        geoCoder: Geocoder
    ): DataConverter {
        return DataConverter(appContext, gson, watchDataStore, geoCoder)
    }

    @Singleton
    @Provides
    fun provideOreoDataConverter(
        @ApplicationContext appContext: Context,
        gson: Gson,
        watchDataStore: WatchDataStore,
        geoCoder: Geocoder
    ): OreoDataConverter {
        return OreoDataConverter(appContext, gson, watchDataStore, geoCoder)
    }

//    @Singleton
//    @Provides
//    fun provideGeocoder1(@ApplicationContext appContext: Context): Geocoder {
//        Locale.setDefault(Locale("en", "GB"))
//        return Geocoder(appContext, Locale.ENGLISH)
//    }

    @Singleton
    @Provides
    fun provideZhUpdateDeviceUnitsHandler(
        dataConverter: DataConverter,
        @ApplicationContext appContext: Context,
        gson: Gson,
        zhApplicationHandler: ZhApplicationHandler,
        watchDataStore: WatchDataStore,
        oreoDataConverter: OreoDataConverter,
    ): ZhUpdateDeviceUnitsHandler {
        return ZhUpdateDeviceUnitsHandler(
            dataConverter,
            oreoDataConverter,
            appContext,
            gson,
            zhApplicationHandler,
            watchDataStore
        )
    }

    @Singleton
    @Provides
    fun provideZhApplicationHandler(): ZhApplicationHandler {
        return ZhApplicationHandler()
    }

    @Singleton
    @Provides
    fun provideZhQueryDeviceUnitsHandler(
        zhApplicationHandler: ZhApplicationHandler,
        dataConverter: DataConverter,
        @ApplicationContext context: Context,
        gson: Gson,
        watchDataStore: WatchDataStore
    ): ZhQueryDeviceUnitsHandler {
        return ZhQueryDeviceUnitsHandler(
            zhApplicationHandler,
            dataConverter,
            context,
            gson,
            watchDataStore
        )
    }

    @Singleton
    @Provides
    fun provideZhUserActivityHandler(
        dataConverter: DataConverter,
        oreoDataConverter: OreoDataConverter,
        zhApplicationHandler: ZhApplicationHandler,
        @ApplicationContext context: Context,
        watchDataStore: WatchDataStore
    ): ZhUserActivityHandler {
        return ZhUserActivityHandler(
            dataConverter,
            oreoDataConverter,
            context,
            watchDataStore,
            zhApplicationHandler
        )
    }

    @Singleton
    @Provides
    fun provideZhConnectHandler(zhApplicationHandler: ZhApplicationHandler): ZhConnectHandler {
        return ZhConnectHandler(zhApplicationHandler)
    }

    @Named("ZhUpdateDevice")
    @Singleton
    @Provides
    fun provideUpdateDeviceDataActions(
        zhUpdateDeviceUnitsHandler: ZhUpdateDeviceUnitsHandler
    ): UpdateDeviceDataActions {
        return zhUpdateDeviceUnitsHandler
    }

    @Named("ZhConnectionDataActions")
    @Singleton
    @Provides
    fun provideConnectionDataActions(zhConnectHandler: ZhConnectHandler): ConnectionDataActions {
        return zhConnectHandler
    }

    @Named("ZhApplicationHandler")
    @Singleton
    @Provides
    fun provideBaseInitializeInterface(zhApplicationHandler: ZhApplicationHandler): BaseInitializeInterface {
        return zhApplicationHandler
    }

    @Named("ZhUserActivityDataActions")
    @Singleton
    @Provides
    fun provideUserActivityDataActions(zhUserActivityHandler: ZhUserActivityHandler): UserActivityDataActions {
        return zhUserActivityHandler
    }

    @Named("ZhQueryDeviceDataActions")
    @Singleton
    @Provides
    fun provideQueryDeviceDataActions(zhQueryDeviceUnitsHandler: ZhQueryDeviceUnitsHandler): QueryDeviceDataActions {
        return zhQueryDeviceUnitsHandler
    }

}