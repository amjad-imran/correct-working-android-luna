package com.noisefit.di

import android.content.Context
import android.location.Geocoder
import com.google.gson.Gson
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.remote.abstraction.DownloadService
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.*
import com.noisefit.data.repository.implementation.*
import com.noisefit.data.repository.pagingSource.TimelinePagingSource
import com.noisefit.util.TestModeUtils
import com.noisefit.watch.*
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.utils.EncryptUtils
import com.oreo.data.dataConverter.OreoOfflineDataMapper
import com.oreo.data.dataConverter.OreoOnlineDataMapper
import com.oreo.data.db.OreoDataBase
import com.oreo.data.db.abstaction.OreoHeartRateDataSource
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.db.implementation.OreoAutoSportDataImpl
import com.oreo.data.db.implementation.OreoBloodOxygenDataImpl
import com.oreo.data.db.implementation.OreoBodyTemperatureDataImpl
import com.oreo.data.db.implementation.OreoDayTimeMovementDataImpl
import com.oreo.data.db.implementation.OreoHeartRateDataImpl
import com.oreo.data.db.implementation.OreoRespiratoryDataImpl
import com.oreo.data.db.implementation.OreoSleepDataImpl
import com.oreo.data.db.implementation.OreoStepsDataImpl
import com.oreo.data.db.implementation.OreoStressDataImpl
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.data.repository.implementation.OreoDeviceRepositoryImpl
import com.oreo.data.repository.implementation.OreoSyncRepositoryImpl
import com.oreo.data.repository.implementation.OreoUserActivityRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext appContext: Context): NoiseFitApplicationMain {
        return appContext as NoiseFitApplicationMain
    }


    @Singleton
    @Provides
    fun provideOreoNetworkEntityMapper(
        stressDataImpl: OreoStressDataImpl,
        respiratoryDataImpl: OreoRespiratoryDataImpl,
        temperatureDataImpl: OreoBodyTemperatureDataImpl,
        oreoHeartRateDataImpl: OreoHeartRateDataImpl,
    ): OreoOnlineDataMapper {
        return OreoOnlineDataMapper(
            stressDataImpl,
            respiratoryDataImpl,
            temperatureDataImpl,
            oreoHeartRateDataImpl
        )
    }

    @Singleton
    @Provides
    fun provideDataUnitConverter(
        localDataStore: DataStoredInterface
    ): DataUnitConverter {
        return DataUnitConverter(localDataStore)
    }


    @Singleton
    @Provides
    fun provideWatches(
        localDataStore: DataStoredInterface,
        ringDataStore: RingDataStore
    ): WatchesSDK {
        return WatchesSDK(localDataStore, ringDataStore)
    }


    @Singleton
    @Provides
    fun provideCallingWatchUtils(
        localDataStore: DataStoredInterface
    ): CallingWatchUtils {
        return CallingWatchUtils(localDataStore)
    }


    /*@Singleton
    @Provides
    fun provideCleverTap(@ApplicationContext appContext: Context): CleverTapAPI? {
        return CleverTapAPI.getDefaultInstance(appContext)
    }
*/

    @Singleton
    @Provides
    fun provideGeocoder(@ApplicationContext appContext: Context): Geocoder {
        Locale.setDefault(Locale("en", "GB"))
        return Geocoder(appContext, Locale.ENGLISH)
    }


    @Singleton
    @Provides
    fun providerAuthenticationRepository(
        remoteDataSource: NetworkService,
//        cleverTapAPI: CleverTapAPI?,
        keyValueDataSource: KeyValueDataSource,
        database: OreoDataBase,
        localDataSource: DataStoredInterface
    ): AuthenticationRepository =
        AuthenticationRepositoryImpl(
            remoteDataSource,
            localDataSource,
            keyValueDataSource,
            database/*cleverTapAPI*/
        )

    @Singleton
    @Provides
    fun providerAppRepository(
        remoteDataSource: NetworkService,
        heartRateDataSource: OreoHeartRateDataImpl,
        stepsDataSource: OreoStepsDataImpl,
        stressDataSource: OreoStressDataImpl,
        bloodOxygenDataSource: OreoBloodOxygenDataImpl,
        tempDataSource: OreoBodyTemperatureDataImpl,
        respDataSource: OreoRespiratoryDataImpl,
        sleepDataSource: OreoSleepDataImpl,
        dayTimeMovementDataSource: OreoDayTimeMovementDataImpl,
        autoWorkoutDataSource: OreoAutoSportDataImpl,
        gson: Gson
    ): AppRepository =
        AppRepositoryImpl(
            remoteDataSource,
            heartRateDataSource,
            stepsDataSource,
            stressDataSource,
            bloodOxygenDataSource,
            tempDataSource,
            respDataSource,
            sleepDataSource,
            dayTimeMovementDataSource,
            autoWorkoutDataSource,
            gson
        )


    @Singleton
    @Provides
    fun providerDeviceRepository(
        remoteDataSource: NetworkService,
        lastSyncProvider: LastSyncProvider
    ): DeviceRepository =
        DeviceRepositoryImpl(remoteDataSource, lastSyncProvider)


    @Singleton
    @Provides
    fun providerUserRepository(
        localDataSource: DataStoredInterface,
        remoteDataSource: NetworkService,
        lastSyncProvider: LastSyncProvider,
        googleFitDataObservers: GoogleFitDataObservers,
        dataUnitConverter: DataUnitConverter,
        offlineDataMapper: OfflineDataMapper
    ): UserRepository =
        UserRepositoryImpl(
            localDataSource,
            remoteDataSource,
            lastSyncProvider,
            offlineDataMapper,
            googleFitDataObservers,
            dataUnitConverter
        )


    @Singleton
    @Provides
    fun providerOreoDeviceRepository(
        localDataSource: DataStoredInterface,
        remoteDataSource: NetworkService,
        gson: Gson
    ): OreoDeviceRepository =
        OreoDeviceRepositoryImpl(
            localDataSource,
            remoteDataSource,
            gson
        )

    @Singleton
    @Provides
    fun providerOreoSyncRepository(
        localDataSource: DataStoredInterface,
        remoteDataSource: NetworkService,
        stepsDataImpl: OreoStepsDataImpl,
        stressDataImpl: OreoStressDataImpl,
        heartRateDataImpl: OreoHeartRateDataImpl,
        bloodOxygenDataImpl: OreoBloodOxygenDataImpl,
        respiratoryDataImpl: OreoRespiratoryDataImpl,
        dayTimeMovementDataImpl: OreoDayTimeMovementDataImpl,
        sleepDataImpl: OreoSleepDataImpl,
        bodyTemperatureDataImpl: OreoBodyTemperatureDataImpl,
        offlineDataMapper: OfflineDataMapper,
        onlineDataMapper: OreoOnlineDataMapper,
        encryptUtils: EncryptUtils,
        lastSyncProvider: LastSyncProvider,
        testModeUtils: TestModeUtils,
        oreoAutoSportDataImpl: OreoAutoSportDataImpl,
        gson: Gson
    ): OreoSyncRepository =
        OreoSyncRepositoryImpl(
            localDataSource,
            remoteDataSource,
            stepsDataImpl,
            stressDataImpl,
            heartRateDataImpl,
            bloodOxygenDataImpl,
            dayTimeMovementDataImpl,
            respiratoryDataImpl,
            sleepDataImpl,
            bodyTemperatureDataImpl,
            offlineDataMapper,
            gson,
            onlineDataMapper,
            encryptUtils,
            lastSyncProvider,
            testModeUtils,
            oreoAutoSportDataImpl
        )


    @Singleton
    @Provides
    fun providerDownloadRepository(
        downloadService: DownloadService
    ): DownloadRepository =
        DownloadRepositoryImpl(downloadService)


    @Singleton
    @Provides
    fun provideApplicationHandler(
        @Named("ZhApplicationHandler")
        zhApplicationHandler: BaseInitializeInterface,
        watchesSdk: WatchesSDK
    ): ApplicationHandler {
        return ApplicationHandler(
            zhApplicationHandler,
            watchesSdk
        )
    }

    @Singleton
    @Provides
    fun provideConnectionHandler(
        @Named("ZhConnectionDataActions")
        zhConnection: ConnectionDataActions,
        watchesSdk: WatchesSDK
    ): ConnectionHandler {
        return ConnectionHandler(
            zhConnection,
            watchesSdk
        )
    }

    @Singleton
    @Provides
    fun provideQueryHandler(
        @Named("ZhQueryDeviceDataActions")
        zhQueryAction: QueryDeviceDataActions,
        watchesSdk: WatchesSDK
    ): DeviceQueryHandler {
        return DeviceQueryHandler(
            zhQueryAction,
            watchesSdk
        )
    }

    @Singleton
    @Provides
    fun provideUpdateDeviceHandler(
        @Named("ZhUpdateDevice")
        zhUpdateDeviceAction: UpdateDeviceDataActions,
        watchesSdk: WatchesSDK
    ): UpdateDeviceHandler {
        return UpdateDeviceHandler(
            zhUpdateDeviceAction,
            watchesSdk
        )
    }

    @Singleton
    @Provides
    fun provideUserActivityHandler(
        @Named("ZhUserActivityDataActions")
        zhUserActivityDataActions: UserActivityDataActions,
        watchesSdk: WatchesSDK
    ): UserActivityHandler {
        return UserActivityHandler(
            zhUserActivityDataActions,
            watchesSdk
        )
    }


    @Singleton
    @Provides
    fun providerOreoUserActivityRepository(

        remoteDataSource: NetworkService,
        gson: Gson,
        localDatSource: DataStoredInterface,
        heartRateDataImpl: OreoHeartRateDataImpl,
        hrv: OreoStressDataImpl,
        bloodOxygenDataImpl: OreoBloodOxygenDataImpl,
        respiratoryDataImpl: OreoRespiratoryDataImpl,
        temperatureDataImpl: OreoBodyTemperatureDataImpl,
        stepsDataImpl: OreoStepsDataImpl,
        sleepDataImpl: OreoSleepDataImpl,
        offlineDataMapper: OreoOfflineDataMapper,
        oreoAutoSportDataImpl: OreoAutoSportDataImpl,
        keyValueDataSource: KeyValueDataSource,
        lastSyncProvider: LastSyncProvider,
        userHealthDataSource: OreoUserHealthDataDataSource,
        offlineApiStore: IOfflineApiResponseStore
    ): OreoUserActivityRepository =
        OreoUserActivityRepositoryImpl(
            remoteDataSource,
            gson,
            localDatSource,
            heartRateDataImpl,
            hrv,
            bloodOxygenDataImpl,
            respiratoryDataImpl,
            temperatureDataImpl,
            sleepDataImpl,
            stepsDataImpl,
            oreoAutoSportDataImpl,
            offlineDataMapper,
            keyValueDataSource,
            userHealthDataSource,
            lastSyncProvider,
            offlineApiStore
        )


    @Singleton
    @Provides
    fun provideTimelinePagingSource(remoteDataSource: NetworkService): TimelinePagingSource =
        TimelinePagingSource(remoteDataSource)

}