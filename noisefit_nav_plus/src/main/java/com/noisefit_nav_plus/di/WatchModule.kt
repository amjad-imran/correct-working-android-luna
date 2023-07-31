package com.noisefit_nav_plus.di

import android.content.Context
import com.google.gson.Gson
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_nav_plus.base.NavPlusApplicationHandler
import com.noisefit_nav_plus.handler.NavPlusQueryDeviceUnitsHandler
import com.noisefit_nav_plus.handler.NavPlusUpdateDeviceUnitsHandler
import com.noisefit_nav_plus.handler.NavPlusUserActivityHandler
import com.noisefit_nav_plus.handler.connect.NavPlusConnectHandler
import com.noisefit_nav_plus.handler.dataConversion.DataConverter

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
    fun provideNavPlusUpdateDeviceUnitsHandler(
        dataConverter: DataConverter,
        @ApplicationContext appContext: Context,
        gson: Gson,
        navPlusApplicationHandler: NavPlusApplicationHandler,
        watchDataStore: WatchDataStore
    ): NavPlusUpdateDeviceUnitsHandler {
        return NavPlusUpdateDeviceUnitsHandler(
            dataConverter,
            appContext,
            gson,
            navPlusApplicationHandler,
            watchDataStore
        )
    }

    @Singleton
    @Provides
    fun provideNavPlusApplicationHandler(): NavPlusApplicationHandler {
        return NavPlusApplicationHandler()
    }

    @Singleton
    @Provides
    fun provideNavPlusQueryDeviceUnitsHandler(
        navPlusApplicationHandler: NavPlusApplicationHandler,
        dataConverter: DataConverter,
        @ApplicationContext context: Context,
        gson: Gson,
        watchDataStore: WatchDataStore
    ): NavPlusQueryDeviceUnitsHandler {
        return NavPlusQueryDeviceUnitsHandler(
            navPlusApplicationHandler,
            dataConverter,
            context,
            gson,
            watchDataStore
        )
    }

    @Singleton
    @Provides
    fun provideNavPlusUserActivityHandler(
        dataConverter: DataConverter,
        navPlusApplicationHandler: NavPlusApplicationHandler
    ): NavPlusUserActivityHandler {
        return NavPlusUserActivityHandler(dataConverter, navPlusApplicationHandler)
    }

    @Singleton
    @Provides
    fun provideNavPlusConnectHandler(navPlusApplicationHandler: NavPlusApplicationHandler): NavPlusConnectHandler {
        return NavPlusConnectHandler(navPlusApplicationHandler)
    }

    @Named("NavPlusUpdateDevice")
    @Singleton
    @Provides
    fun provideUpdateDeviceDataActions(
        navPlusUpdateDeviceUnitsHandler: NavPlusUpdateDeviceUnitsHandler
    ): UpdateDeviceDataActions {
        return navPlusUpdateDeviceUnitsHandler
    }

    @Named("NavPlusConnectionDataActions")
    @Singleton
    @Provides
    fun provideConnectionDataActions(navPlusConnectHandler: NavPlusConnectHandler): ConnectionDataActions {
        return navPlusConnectHandler
    }

    @Named("NavPlusApplicationHandler")
    @Singleton
    @Provides
    fun provideBaseInitializeInterface(navPlusApplicationHandler: NavPlusApplicationHandler): BaseInitializeInterface {
        return navPlusApplicationHandler
    }

    @Named("NavPlusUserActivityDataActions")
    @Singleton
    @Provides
    fun provideUserActivityDataActions(navPlusUserActivityHandler: NavPlusUserActivityHandler): UserActivityDataActions {
        return navPlusUserActivityHandler
    }

    @Named("NavPlusQueryDeviceDataActions")
    @Singleton
    @Provides
    fun provideQueryDeviceDataActions(navPlusQueryDeviceUnitsHandler: NavPlusQueryDeviceUnitsHandler): QueryDeviceDataActions {
        return navPlusQueryDeviceUnitsHandler
    }

}