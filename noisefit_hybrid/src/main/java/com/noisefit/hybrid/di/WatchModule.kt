package com.noisefit.hybrid.di

import android.content.Context
import com.noisefit.hybrid.base.NFHybridApplicationHandler
import com.noisefit.hybrid.base.VisionCommands
import com.noisefit.hybrid.dataconversions.DataConverter
import com.noisefit.hybrid.handler.NFHQueryDeviceUnitsHandler
import com.noisefit.hybrid.handler.NFHUpdateDeviceUnitsHandler
import com.noisefit.hybrid.handler.NFHUserActivityHandler
import com.noisefit.hybrid.handler.connect.NFHybridConnectHandler
import com.noisefit.hybrid.utils.*
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


    @Named("NFHybridApplicationHandler")
    @Singleton
    @Provides
    fun provideBaseInitializeInterface(handler: NFHybridApplicationHandler): BaseInitializeInterface {
        return handler
    }


    @Singleton
    @Provides
    fun provideVisionCommand(bitwiseUtils: BitwiseUtils): VisionCommands {
        return VisionCommands(bitwiseUtils)
    }

    @Singleton
    @Provides
    fun provideVisionHelperMethods(
        bitwiseHelperUtils: BitwiseHelperUtils,
        visionCommands: VisionCommands
    ): VisionHelperMethods {
        return VisionHelperMethods(bitwiseHelperUtils, visionCommands)
    }

    @Singleton
    @Provides
    fun provideBitwiseUtils(): BitwiseUtils {
        return BitwiseUtils()
    }

    @Singleton
    @Provides
    fun provideOnlineWatchFacesVision(): OnlineWatchFacesVision {
        return OnlineWatchFacesVision()
    }

    @Singleton
    @Provides
    fun provideBluetoothSDKExp(): BluetoothSDK_Exp {
        return BluetoothSDK_Exp()
    }

    @Singleton
    @Provides
    fun provideNFHybridDataConverter(): DataConverter {
        return DataConverter()
    }


    @Singleton
    @Provides
    fun provideBitwiseHelperUtils(
        bitwiseUtils: BitwiseUtils,
        dataConverter: DataConverter,
        visionCommands: VisionCommands
    ): BitwiseHelperUtils {
        return BitwiseHelperUtils(bitwiseUtils, dataConverter, visionCommands)
    }

    @Singleton
    @Provides
    fun provideApplicationHandler(): NFHybridApplicationHandler {
        return NFHybridApplicationHandler()
    }

    @Singleton
    @Provides
    fun provideNFHQueryDeviceUnitsHandler(
        bitwiseUtils: BitwiseUtils,
        dataConverter: DataConverter,
        bitwiseHelperUtils: BitwiseHelperUtils,
        watchDataStore: WatchDataStore,
        visionCommands: VisionCommands,
        @ApplicationContext appContext: Context,
        bluetoothsdkExp: BluetoothSDK_Exp,
    ): NFHQueryDeviceUnitsHandler {
        return NFHQueryDeviceUnitsHandler(
            bitwiseUtils,
            dataConverter,
            bitwiseHelperUtils,
            watchDataStore,
            visionCommands,
            appContext,
            bluetoothsdkExp
        )
    }

    @Singleton
    @Provides
    fun provideNavPlusConnectHandler(applicationHandler: NFHybridApplicationHandler): NFHybridConnectHandler {
        return NFHybridConnectHandler(applicationHandler)
    }

    @Named("NFHConnectionDataActions")
    @Singleton
    @Provides
    fun provideConnectionDataActions(nfhConnectHandler: NFHybridConnectHandler): ConnectionDataActions {
        return nfhConnectHandler
    }

    @Named("NFHQueryDeviceDataActions")
    @Singleton
    @Provides
    fun provideQueryDeviceDataActions(nfhQueryDeviceUnitsHandler: NFHQueryDeviceUnitsHandler): QueryDeviceDataActions {
        return nfhQueryDeviceUnitsHandler
    }

    @Named("NFHUpdateDevice")
    @Singleton
    @Provides
    fun provideUpdateDeviceDataActions(
        nfhDeviceUnitsHandler: NFHUpdateDeviceUnitsHandler
    ): UpdateDeviceDataActions {
        return nfhDeviceUnitsHandler
    }

    @Singleton
    @Provides
    fun provideNfhUpdateDeviceUnitsHandler(
        @ApplicationContext appContext: Context,
        bitwiseUtils: BitwiseUtils,
        dataConverter: DataConverter,
        bitwiseHelperUtils: BitwiseHelperUtils,
        watchDataStore: WatchDataStore,
        onlineWatchFacesVision: OnlineWatchFacesVision,
        bluetoothsdkExp: BluetoothSDK_Exp,
        visionCommands: VisionCommands
    ): NFHUpdateDeviceUnitsHandler {
        return NFHUpdateDeviceUnitsHandler(
            appContext,
            bitwiseUtils,
            dataConverter,
            bitwiseHelperUtils,
            watchDataStore,
            onlineWatchFacesVision,
            bluetoothsdkExp,
            visionCommands
        )
    }

    @Singleton
    @Provides
    fun provideNFHUserActivityDataHandler(
        applicationHandler: NFHybridApplicationHandler,
        dataConverter: DataConverter,
        visionHelperMethods: VisionHelperMethods
    ): NFHUserActivityHandler {
        return NFHUserActivityHandler(
            applicationHandler,
            dataConverter,
            visionHelperMethods
        )
    }

    @Named("NFHUserActivityDataActions")
    @Singleton
    @Provides
    fun provideUserActivityDataActions(nfhUserActivityDataHandler: NFHUserActivityHandler): UserActivityDataActions {
        return nfhUserActivityDataHandler
    }

}