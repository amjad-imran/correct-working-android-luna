package com.noisefit.di

import com.noisefit.data.remote.abstraction.ExternalFirmwareService
import com.noisefit.data.remote.abstraction.WeatherService
import com.noisefit.data.repository.abstraction.ExternalFirmwareRepository
import com.noisefit.data.repository.implementation.ExternalFirmwareRepositoryImpl
import com.noisefit.data.repository.implementation.WeatherRepository
import com.noisefit.data.repository.implementation.WeatherRepositoryImpl
import com.noisefit.session.SessionManager
import com.noisefit.watch.WatchesSDK
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExternalModule {


    @Singleton
    @Provides
    fun providerExternalFirmwareRepository(firmwareService: ExternalFirmwareService): ExternalFirmwareRepository =
        ExternalFirmwareRepositoryImpl(firmwareService)

    @Singleton
    @Provides
    fun providerWeatherRepository(weatherDataSource: WeatherService,
                                  sessionManager: SessionManager,
                                  watchesSDK: WatchesSDK
    ): WeatherRepository =
        WeatherRepositoryImpl(weatherDataSource)


}