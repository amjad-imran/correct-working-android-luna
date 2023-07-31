package com.noisefit.di

import com.noisefit.data.remote.abstraction.ExternalFirmwareService
import com.noisefit.data.remote.abstraction.StockService
import com.noisefit.data.remote.abstraction.WeatherService
import com.noisefit.data.repository.abstraction.ExternalFirmwareRepository
import com.noisefit.data.repository.abstraction.StockRepository
import com.noisefit.data.repository.abstraction.WeatherRepository
import com.noisefit.data.repository.implementation.ExternalFirmwareRepositoryImpl
import com.noisefit.data.repository.implementation.StockRepositoryImpl
import com.noisefit.data.repository.implementation.WeatherRepositoryImpl
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
    fun providerWeatherRepository(weatherDataSource: WeatherService): WeatherRepository =
        WeatherRepositoryImpl(weatherDataSource)

    @Singleton
    @Provides
    fun providerStockRepository(stockDataSource: StockService): StockRepository =
        StockRepositoryImpl(stockDataSource)



    @Singleton
    @Provides
    fun providerExternalFirmwareRepository(firmwareService: ExternalFirmwareService): ExternalFirmwareRepository =
        ExternalFirmwareRepositoryImpl(firmwareService)



}