package com.noisefit.colorfit_pro.di

import android.content.Context
import com.noisefit.colorfit_pro.base.ProApplicationHandler
import com.noisefit.colorfit_pro.dataConversion.DataConverter
import com.noisefit.colorfit_pro.handler.ProQueryDeviceUnitsHandler
import com.noisefit.colorfit_pro.handler.ProUpdateDeviceUnitsHandler
import com.noisefit.colorfit_pro.handler.ProUserActivityHandler
import com.noisefit.colorfit_pro.handler.connect.ProConnectHandler
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
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
        @ApplicationContext appContext: Context
    ): DataConverter {
        return DataConverter(appContext)
    }

    @Singleton
    @Provides
    fun provideProApplicationHandler(@ApplicationContext appContext: Context): ProApplicationHandler {
        return ProApplicationHandler(appContext)
    }

    @Named("ProApplicationHandler")
    @Singleton
    @Provides
    fun provideBaseInitializeInterface(proApplicationHandler: ProApplicationHandler): BaseInitializeInterface {
        return proApplicationHandler
    }

    @Singleton
    @Provides
    fun provideProConnectHandler(proApplicationHandler: ProApplicationHandler): ProConnectHandler {
        return ProConnectHandler(proApplicationHandler)
    }


    @Singleton
    @Provides
    fun provideProQueryDeviceUnitsHandler(
        proApplicationHandler: ProApplicationHandler,
        dataConverter: DataConverter,
        @ApplicationContext context: Context,
        watchDataStore: WatchDataStore
    ): ProQueryDeviceUnitsHandler {
        return ProQueryDeviceUnitsHandler(
            proApplicationHandler,
            dataConverter,
            context,
            watchDataStore
        )
    }

    @Named("ProActivityDataActions")
    @Singleton
    @Provides
    fun provideUserActivityDataActions(proUserActivityHandler: ProUserActivityHandler): UserActivityDataActions {
        return proUserActivityHandler
    }

    @Singleton
    @Provides
    fun provideProUserActivityHandler(
        dataConverter: DataConverter,
        proApplicationHandler: ProApplicationHandler
    ): ProUserActivityHandler {
        return ProUserActivityHandler(dataConverter, proApplicationHandler)
    }


    @Named("ProConnectionDataActions")
    @Singleton
    @Provides
    fun provideConnectionDataActions(proConnectHandler: ProConnectHandler): ConnectionDataActions {
        return proConnectHandler
    }


    @Named("ProQueryDeviceDataActions")
    @Singleton
    @Provides
    fun provideProDeviceDataActions(proQueryDeviceUnitsHandler: ProQueryDeviceUnitsHandler): QueryDeviceDataActions {
        return proQueryDeviceUnitsHandler
    }

    @Singleton
    @Provides
    fun provideProUpdateDeviceUnitsHandler(
        dataConverter: DataConverter,
        @ApplicationContext appContext: Context,
        proConnectHandler: ProConnectHandler,
        proApplicationHandler: ProApplicationHandler,
        watchDataStore: WatchDataStore
    ): ProUpdateDeviceUnitsHandler {
        return ProUpdateDeviceUnitsHandler(
            dataConverter,
            appContext,
            proConnectHandler,
            proApplicationHandler,
            watchDataStore
        )
    }

    @Named("ProUpdateDevice")
    @Singleton
    @Provides
    fun provideUpdateDeviceDataActions(
        proUpdateDeviceUnitsHandler: ProUpdateDeviceUnitsHandler
    ): UpdateDeviceDataActions {
        return proUpdateDeviceUnitsHandler
    }
}