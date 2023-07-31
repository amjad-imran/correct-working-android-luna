package com.noisefit_cf2.di

import com.noisefit_cf2.handler.CF2UserActivityDataHandler
import com.noisefit_cf2.base.ColorFit2ApplicationHandler
import com.noisefit_cf2.dataconversions.Colorfit2DataConverter
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_cf2.handler.CF2QueryDeviceUnitsHandler
import com.noisefit_cf2.handler.CF2UpdateDeviceUnitsHandler
import com.noisefit_cf2.handler.connect.CF2ConnectHandler
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WatchModule {

    @Singleton
    @Provides
    fun provideDataConverter(): Colorfit2DataConverter {
        return Colorfit2DataConverter()
    }

    @Named("ColorFit2ApplicationHandler")
    @Singleton
    @Provides
    fun provideBaseInitializeInterface(handler: ColorFit2ApplicationHandler): BaseInitializeInterface {
        return handler
    }

    @Singleton
    @Provides
    fun provideApplicationHandler(watchDataStore: WatchDataStore): ColorFit2ApplicationHandler {
        return ColorFit2ApplicationHandler(watchDataStore)
    }

    @Singleton
    @Provides
    fun provideCF2QueryDeviceUnitsHandler(
        watchDataStore : WatchDataStore
    ): CF2QueryDeviceUnitsHandler {
        return CF2QueryDeviceUnitsHandler(watchDataStore)
    }

    @Singleton
    @Provides
    fun provideNavPlusConnectHandler(applicationHandler: ColorFit2ApplicationHandler): CF2ConnectHandler {
        return CF2ConnectHandler(applicationHandler)
    }

    @Singleton
    @Provides
    fun provideCF2UserActivityDataHandler(
        dataConverter: Colorfit2DataConverter,
        colorFit2ApplicationHandler: ColorFit2ApplicationHandler
    ): CF2UserActivityDataHandler {
        return CF2UserActivityDataHandler(dataConverter, colorFit2ApplicationHandler)
    }

    @Named("ColorFit2ConnectionDataActions")
    @Singleton
    @Provides
    fun provideConnectionDataActions(cF2ConnectHandler: CF2ConnectHandler): ConnectionDataActions {
        return cF2ConnectHandler
    }

    @Named("CF2QueryDeviceDataActions")
    @Singleton
    @Provides
    fun provideQueryDeviceDataActions(cf2QueryDeviceUnitsHandler: CF2QueryDeviceUnitsHandler): QueryDeviceDataActions {
        return cf2QueryDeviceUnitsHandler
    }

    @Named("Cf2UpdateDevice")
    @Singleton
    @Provides
    fun provideUpdateDeviceDataActions(
        cf2DeviceUnitsHandler: CF2UpdateDeviceUnitsHandler
    ): UpdateDeviceDataActions {
        return cf2DeviceUnitsHandler
    }

    @Singleton
    @Provides
    fun provideCf2UpdateDeviceUnitsHandler(
        watchDataStore: WatchDataStore
    ): CF2UpdateDeviceUnitsHandler {
        return CF2UpdateDeviceUnitsHandler(watchDataStore)
    }

    @Named("CF2UserActivityDataActions")
    @Singleton
    @Provides
    fun provideUserActivityDataActions(cF2UserActivityDataHandler: CF2UserActivityDataHandler): UserActivityDataActions {
        return cF2UserActivityDataHandler
    }

}