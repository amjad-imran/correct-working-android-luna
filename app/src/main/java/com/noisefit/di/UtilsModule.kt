package com.noisefit.di

import com.noisefit.session.SessionManager
import com.noisefit.util.*
import com.noisefit.util.ImageUtil
import com.noisefit_commans.utils.autostart.AutoStartUtil
import com.noisefit.util.moveToServer.BatteryNotificationUtils
import com.noisefit.util.moveToServer.SleepNotificationUtils
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {


    @Singleton
    @Provides
    fun provideVibrationUtils(): VibrationUtils {
        return VibrationUtils()
    }

    @Singleton
    @Provides
    fun provideScreenUtils(): ScreenUtils {
        return ScreenUtils()
    }


    @Singleton
    @Provides
    fun provideGoogleMapsUtil(): GoogleMapsUtil {
        return GoogleMapsUtil()
    }

    @Singleton
    @Provides
    fun provideEncryptUtils(): EncryptUtils {
        return EncryptUtils()
    }

    @Singleton
    @Provides
    fun provideDeviceUtil(): DeviceUtil {
        return DeviceUtil()
    }

    @Singleton
    @Provides
    fun provideConnectionUtil(): ConnectionUtil {
        return ConnectionUtil()
    }


    @Singleton
    @Provides
    fun provideCallHandler(): CallHandler {
        return CallHandler()
    }

    @Singleton
    @Provides
    fun provideAutoStartUtil(): AutoStartUtil {
        return AutoStartUtil()
    }

    @Singleton
    @Provides
    fun provideImageUtil(): ImageUtil {
        return ImageUtil()
    }

    @Singleton
    @Provides
    fun provideBatteryNotificationUtils(
        localDataStore: DataStoredInterface,
        sessionManager: SessionManager
    ): BatteryNotificationUtils {
        return BatteryNotificationUtils(localDataStore, sessionManager)
    }

    @Singleton
    @Provides
    fun provideSleepLocalNotificationUtils(localDataStore: DataStoredInterface): SleepNotificationUtils {
        return SleepNotificationUtils(localDataStore)
    }

    @Singleton
    @Provides
    fun provideTestMode(
        watchesSdk: WatchesSDK,
        localDataStore: DataStoredInterface
    ): TestModeUtils {
        return TestModeUtils(watchesSdk, localDataStore)
    }
}
