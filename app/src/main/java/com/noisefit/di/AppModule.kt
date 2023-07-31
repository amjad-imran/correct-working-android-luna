package com.noisefit.di

import android.content.Context
import android.location.Geocoder
import com.google.gson.Gson
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.dataConverter.OnlineDataMapper
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.noisefit.data.local.db.abstraction.FeedsDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.database.ActivityDao
import com.noisefit.data.local.db.implementation.*
import com.noisefit.data.remote.abstraction.DownloadService
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.abstraction.ShopService
import com.noisefit.data.remote.abstraction.SportService
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.*
import com.noisefit.data.repository.implementation.*
import com.noisefit.data.repository.pagingSource.TimelinePagingSource
import com.noisefit.session.SessionManager
import com.noisefit.util.FilterUtils
import com.noisefit.util.SportUtils
import com.noisefit.util.TestModeUtils
import com.noisefit.watch.*
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.base.BaseInitializeInterface
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.utils.EncryptUtils
import com.oreo.data.dataConverter.OreoOfflineDataMapper
import com.oreo.data.dataConverter.OreoOnlineDataMapper
import com.oreo.data.db.implementation.OreoBloodOxygenDataImpl
import com.oreo.data.db.implementation.OreoBodyTemperatureDataImpl
import com.oreo.data.db.implementation.OreoDayTimeMovementDataImpl
import com.oreo.data.db.implementation.OreoHeartRateDataImpl
import com.oreo.data.db.implementation.OreoRespiratoryDataImpl
import com.oreo.data.db.implementation.OreoSleepDataImpl
import com.oreo.data.db.implementation.OreoStepsDataImpl
import com.oreo.data.db.implementation.OreoStressDataImpl
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
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

    @Named("DbDataConverter")
    @Singleton
    @Provides
    fun provideDataConverter(
        watches: WatchesSDK,
        stressDataImpl: StressDataImpl,
        heartRateDataImpl: HeartRateDataImpl
    ): OfflineDataMapper {
        return OfflineDataMapper(watches, stressDataImpl, heartRateDataImpl)
    }

    @Singleton
    @Provides
    fun provideEntityMapper(
        stressDataImpl: StressDataImpl,
        heartRateDataImpl: HeartRateDataImpl,
        oreoHeartRateDataImpl: OreoHeartRateDataImpl,
    ): OnlineDataMapper {
        return OnlineDataMapper(stressDataImpl, heartRateDataImpl, oreoHeartRateDataImpl)
    }

    @Singleton
    @Provides
    fun provideOreoNetworkEntityMapper(
        stressDataImpl: OreoStressDataImpl,
        oreoHeartRateDataImpl: OreoHeartRateDataImpl,
    ): OreoOnlineDataMapper {
        return OreoOnlineDataMapper(stressDataImpl, oreoHeartRateDataImpl)
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
        localDataStore: DataStoredInterface
    ): WatchesSDK {
        return WatchesSDK(localDataStore)
    }

    @Singleton
    @Provides
    fun provideSportUtils(
        localDataStore: DataStoredInterface,
        sessionManager: SessionManager,
        sportEventRepository: SportEventRepository
    ): SportUtils {
        return SportUtils(localDataStore, sessionManager, sportEventRepository)
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
        localDataSource: DataStoredInterface
    ): AuthenticationRepository =
        AuthenticationRepositoryImpl(remoteDataSource, localDataSource /*cleverTapAPI*/)

    @Singleton
    @Provides
    fun providerAppRepository(
        remoteDataSource: NetworkService,
        stepsDataImpl: StepsDataImpl,
        stressDataImpl: StressDataImpl,
        heartRateDataImpl: HeartRateDataImpl,
        bloodOxygenDataImpl: BloodOxygenDataImpl,
        sleepDataImpl: SleepDataImpl,
        bodyTemperatureDataImpl: BodyTemperatureDataImpl,
        sportEventDataImpl: SportEventDataImpl,
        lastSyncProvider: LastSyncProvider,
        offlineApiStore: IOfflineApiResponseStore,
        keyValueDataSource: KeyValueDataSource,
        gson: Gson
    ): AppRepository =
        AppRepositoryImpl(
            remoteDataSource,
            stepsDataImpl,
            stressDataImpl,
            heartRateDataImpl,
            bloodOxygenDataImpl,
            sleepDataImpl,
            bodyTemperatureDataImpl,
            sportEventDataImpl,
            lastSyncProvider,
            offlineApiStore,
            keyValueDataSource,
            gson
        )

    @Singleton
    @Provides
    fun providerFriendsRepository(
        remoteDataSource: NetworkService, gson: Gson, keyValueDataSource: KeyValueDataSource,
        lastSyncProvider: LastSyncProvider,
        localDataSource: DataStoredInterface,
        userActivityRepository: UserActivityRepository
    ): FriendsRepository =
        FriendsRepositoryImpl(
            remoteDataSource,
            gson,
            keyValueDataSource,
            lastSyncProvider,
            localDataSource,
            userActivityRepository
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
    fun providerWatchFaceRepository(
        keyValueDataSource: KeyValueDataSource,
        remoteDataSource: NetworkService,
        watchFaceDataImpl: WatchFaceDataSourceImpl,
        lastSyncProvider: LastSyncProvider,
        gson: Gson,
    ): WatchFaceRepository =
        WatchFaceRepositoryImpl(
            gson,
            keyValueDataSource,
            remoteDataSource,
            lastSyncProvider,
            watchFaceDataImpl
        )

    @Singleton
    @Provides
    fun providerShopRepository(shopService: ShopService): ShopRepository =
        ShopRepositoryImpl(shopService)

    @Singleton
    @Provides
    fun providerSportRepository(
        sportService: SportService,
        sportEventDataImpl: SportEventDataImpl
    ): SportEventRepository =
        SportEventRepositoryImpl(sportService, sportEventDataImpl)


    @Singleton
    @Provides
    fun providerUserRepository(
        localDataSource: DataStoredInterface,
        remoteDataSource: NetworkService,
        activityDao: ActivityDao,
        stepsDataImpl: StepsDataImpl,
        stressDataImpl: StressDataImpl,
        heartRateDataImpl: HeartRateDataImpl,
        bloodOxygenDataImpl: BloodOxygenDataImpl,
        lastSyncProvider: LastSyncProvider,
        sleepDataImpl: SleepDataImpl,
        googleFitDataObservers: GoogleFitDataObservers,
        bodyTemperatureDataImpl: BodyTemperatureDataImpl,
        userActivityRepository: UserActivityRepository,
        dataUnitConverter: DataUnitConverter,
        @Named("DbDataConverter")
        offlineDataMapper: OfflineDataMapper
    ): UserRepository =
        UserRepositoryImpl(
            localDataSource,
            remoteDataSource,
            activityDao,
            stepsDataImpl,
            stressDataImpl,
            lastSyncProvider,
            heartRateDataImpl,
            bloodOxygenDataImpl,
            sleepDataImpl,
            bodyTemperatureDataImpl,
            offlineDataMapper,
            googleFitDataObservers,
            dataUnitConverter,
            userActivityRepository
        )

    @Singleton
    @Provides
    fun providerSyncRepository(
        localDataSource: DataStoredInterface,
        remoteDataSource: NetworkService,
        stepsDataImpl: StepsDataImpl,
        stressDataImpl: StressDataImpl,
        heartRateDataImpl: HeartRateDataImpl,
        bloodOxygenDataImpl: BloodOxygenDataImpl,
        sleepDataImpl: SleepDataImpl,
        googleFitDataImpl: GoogleFitDataImpl,
        bodyTemperatureDataImpl: BodyTemperatureDataImpl,
        @Named("DbDataConverter")
        offlineDataMapper: OfflineDataMapper,
        onlineDataMapper: OnlineDataMapper,
        encryptUtils: EncryptUtils,
        lastSyncProvider: LastSyncProvider,
        userActivityRepository: UserActivityRepository,
        testModeUtils: TestModeUtils,
        gson: Gson
    ): SyncRepository =
        SyncRepositoryImpl(
            localDataSource,
            remoteDataSource,
            stepsDataImpl,
            stressDataImpl,
            heartRateDataImpl,
            bloodOxygenDataImpl,
            sleepDataImpl,
            googleFitDataImpl,
            bodyTemperatureDataImpl,
            offlineDataMapper,
            gson,
            onlineDataMapper,
            encryptUtils,
            lastSyncProvider,
            userActivityRepository,
            testModeUtils
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
        googleFitDataImpl: GoogleFitDataImpl,
        bodyTemperatureDataImpl: OreoBodyTemperatureDataImpl,
        @Named("DbDataConverter")
        offlineDataMapper: OfflineDataMapper,
        onlineDataMapper: OreoOnlineDataMapper,
        encryptUtils: EncryptUtils,
        lastSyncProvider: LastSyncProvider,
        userActivityRepository: UserActivityRepository,
        testModeUtils: TestModeUtils,
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
            googleFitDataImpl,
            bodyTemperatureDataImpl,
            offlineDataMapper,
            gson,
            onlineDataMapper,
            encryptUtils,
            lastSyncProvider,
            userActivityRepository,
            testModeUtils
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
    fun providerUserActivityRepository(
        offlineApiResponseStore: IOfflineApiResponseStore,
        lastSyncProvider: LastSyncProvider,
        remoteDataSource: NetworkService,
        keyValueDataSource: KeyValueDataSource,
        gson: Gson
    ): UserActivityRepository =
        UserActivityRepositoryImpl(
            offlineApiResponseStore,
            remoteDataSource,
            lastSyncProvider,
            keyValueDataSource,
            gson
        )

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
            offlineDataMapper
        )


    @Singleton
    @Provides
    fun providerRewardsRepository(
        remoteDataSource: NetworkService,
        keyValueDataSource: KeyValueDataSource,
        userActivityRepository: UserActivityRepository,
        gson: Gson

    ): RewardsRepository =
        RewardsRepositoryImpl(
            remoteDataSource,
            keyValueDataSource,
            userActivityRepository,
            gson
        )

    @Singleton
    @Provides
    fun provideContentRepository(
        remoteDataSource: NetworkService
    ): ContentRepository = ContentRepositoryImpl(remoteDataSource)

    @Singleton
    @Provides
    fun provideFeedRepository(
        remoteDataSource: NetworkService,
        localDataSource: DataStoredInterface,
        timelinePagingSource: TimelinePagingSource,
        gson: Gson,
        feedDbSource: FeedsDataSource
    ): FeedRepository =
        FeedRepositoryImpl(
            remoteDataSource,
            localDataSource,
            timelinePagingSource,
            gson,
            feedDbSource
        )

    @Singleton
    @Provides
    fun provideTimelinePagingSource(remoteDataSource: NetworkService): TimelinePagingSource =
        TimelinePagingSource(remoteDataSource)

    @Singleton
    @Provides
    fun providerNPLRepository(
        remoteDataSource: NetworkService,
        userActivityRepository: UserActivityRepository,
    ): NPLRepository = NPLRepositoryImpl(remoteDataSource, userActivityRepository)


    @Singleton
    @Provides
    fun provideFilerUtils(): FilterUtils = FilterUtils()

}