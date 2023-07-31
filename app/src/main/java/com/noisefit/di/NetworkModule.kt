package com.noisefit.di

import android.content.Context
import com.noisefit.BuildConfig
import com.noisefit.data.local.db.abstraction.FeedsDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.remote.NetworkConnectionInterceptor
import com.noisefit.data.remote.NetworkConnectionInterceptorShop
import com.noisefit.data.remote.NetworkConstants
import com.noisefit.data.remote.abstraction.*
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.RingDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideRetroFit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()


    @Singleton
    @Provides
    fun buildTokenApi(@Named("TokenClient") client: OkHttpClient): TokenRefreshApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TokenRefreshApi::class.java)
    }

    @Singleton
    @Provides
    fun provideHttpClient(
        networkConnectionInterceptor: NetworkConnectionInterceptor,
        logger: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(networkConnectionInterceptor)
        //.authenticator(authenticator)
        .readTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                this.addInterceptor(logger)
            }
        }
        .build()


    @Named("TokenClient")
    @Singleton
    @Provides
    fun provideHttpClientAuthenticator(
        logger: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                this.addInterceptor(logger)
            }
        }
        .build()

    @Named("NwInterceptorShop")
    @Singleton
    @Provides
    fun provideNetworkInterceptorShop(
        @ApplicationContext appContext: Context
    ): NetworkConnectionInterceptorShop =
        NetworkConnectionInterceptorShop(appContext)

    @Named("HttpClientShop")
    @Singleton
    @Provides
    fun provideHttpClientShop(
        @Named("NwInterceptorShop") networkConnectionInterceptor: NetworkConnectionInterceptorShop,
        logger: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(networkConnectionInterceptor)
        .readTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                this.addInterceptor(logger)
            }
        }
        .build()


    @Named("HttpClientSport")
    @Singleton
    @Provides
    fun provideHttpClientSport(
        @Named("NwInterceptorShop") networkConnectionInterceptor: NetworkConnectionInterceptorShop,
        logger: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(networkConnectionInterceptor)
        .readTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(NetworkConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                this.addInterceptor(logger)
            }
        }
        .build()


    @Singleton
    @Provides
    fun provideLoginInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(
        HttpLoggingInterceptor.Level.BODY
    )

    @Singleton
    @Provides
    fun provideNetworkInterceptor(
        @ApplicationContext appContext: Context,
        localDataStore: DataStoredInterface,
        ringDataStore: RingDataStore,
        lastSyncProvider: LastSyncProvider,
        keyValueDataSource: KeyValueDataSource,
        feedsDBSource: FeedsDataSource,
        watchesSdk: WatchesSDK,
        tokenService: TokenRefreshApi
    ): NetworkConnectionInterceptor =
        NetworkConnectionInterceptor(
            appContext,
            lastSyncProvider,
            localDataStore,
            ringDataStore,
            keyValueDataSource,
            feedsDBSource,
            watchesSdk,
            tokenService
        )

    @Singleton
    @Provides
    fun provideRestService(retrofit: Retrofit): NetworkService =
        retrofit.create(NetworkService::class.java)


    @Named("RetrofitShop")
    @Singleton
    @Provides
    fun provideShopRetroFit(@Named("HttpClientShop") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.SHOP_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    @Named("RetrofitSport")
    @Singleton
    @Provides
    fun provideSportApi(@Named("HttpClientSport") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL_WEATHER)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    @Named("RetrofitWeather")
    @Singleton
    @Provides
    fun provideWeatherRetroFit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL_WEATHER)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()


    @Named("RetrofitStock")
    @Singleton
    @Provides
    fun provideStockRetroFit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL_STOCK)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    @Named("RetrofitDownload")
    @Singleton
    @Provides
    fun provideDownloadRetroFit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .client(client)
        .build()


    @Singleton
    @Provides
    fun provideWeatherRestService(@Named("RetrofitWeather") retrofit: Retrofit): WeatherService =
        retrofit.create(WeatherService::class.java)

    @Singleton
    @Provides
    fun provideShopRestService(@Named("RetrofitShop") retrofit: Retrofit): ShopService =
        retrofit.create(ShopService::class.java)

    @Singleton
    @Provides
    fun provideSportRestService(@Named("RetrofitSport") retrofit: Retrofit): SportService =
        retrofit.create(SportService::class.java)

    @Singleton
    @Provides
    fun provideStockRestService(@Named("RetrofitStock") retrofit: Retrofit): StockService =
        retrofit.create(StockService::class.java)

    @Singleton
    @Provides
    fun provideDownloadService(@Named("RetrofitDownload") retrofit: Retrofit): DownloadService =
        retrofit.create(DownloadService::class.java)

}