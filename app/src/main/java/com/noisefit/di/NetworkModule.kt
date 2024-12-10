package com.noisefit.di

import android.content.Context
import com.grapesnberries.curllogger.CurlLoggerInterceptor
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.remote.HeaderInterceptorAudio
import com.noisefit.luna.BuildConfig
import com.noisefit.data.remote.NetworkConnectionInterceptor
import com.noisefit.data.remote.NetworkConnectionInterceptorShop
import com.noisefit.data.remote.NetworkConstants
import com.noisefit.data.remote.abstraction.*
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.tryCatch
import com.oreo.data.db.OreoDataBase
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
    fun buildAudioApi(@Named("AudioClient") client: OkHttpClient): AudioApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL_NEW)
            .client(client)
            .build()
            .create(AudioApiService::class.java)
    }

    @Named("AudioClient")
    @Singleton
    @Provides
    fun provideHttpClientAudio(
        @Named("NwInterceptorAudio") networkConnectionInterceptor: HeaderInterceptorAudio,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(networkConnectionInterceptor)
        .apply {
            if (BuildConfig.DEBUG) {
                tryCatch {
                    this.addInterceptor(CurlLoggerInterceptor("CURL"))
                }
            }
        }
        .build()


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
                tryCatch {
                    this.addInterceptor(CurlLoggerInterceptor("CURL"))
                }
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

    @Named("NwInterceptorAudio")
    @Singleton
    @Provides
    fun provideNetworkInterceptorAudio(
        @ApplicationContext appContext: Context,
        localDataStore: DataStoredInterface,
        ringDataStore: RingDataStore,
    ): HeaderInterceptorAudio =
        HeaderInterceptorAudio(appContext, localDataStore, ringDataStore)


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
        watchDataStore: WatchDataStore,
        lastSyncProvider: LastSyncProvider,
        watchesSdk: WatchesSDK,
        resourcesProvider: ResourcesProvider,
        keyValueDataSource: KeyValueDataSource,
        database: OreoDataBase,
        tokenService: TokenRefreshApi,
    ): NetworkConnectionInterceptor =
        NetworkConnectionInterceptor(
            appContext,
            lastSyncProvider,
            localDataStore,
            ringDataStore,
            watchDataStore,
            resourcesProvider,
            watchesSdk,
            keyValueDataSource,
            database,
            tokenService
        )

    @Singleton
    @Provides
    fun provideRestService(retrofit: Retrofit): NetworkService =
        retrofit.create(NetworkService::class.java)

    @Named("RetrofitWeather")
    @Singleton
    @Provides
    fun provideWeatherRetroFit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL_WEATHER)
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
    fun provideDownloadService(@Named("RetrofitDownload") retrofit: Retrofit): DownloadService =
        retrofit.create(DownloadService::class.java)

}