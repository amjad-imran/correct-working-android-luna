package com.noisefit_ryeex_sdk.di;

import android.content.Context;
import com.google.gson.Gson;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import com.noisefit_ryeex_sdk.dataConversion.DataConverter;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class WatchModule_ProvideDataConverterFactory implements Factory<DataConverter> {
  private final Provider<Context> appContextProvider;

  private final Provider<Gson> gsonProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideDataConverterFactory(Provider<Context> appContextProvider,
      Provider<Gson> gsonProvider, Provider<WatchDataStore> watchDataStoreProvider) {
    this.appContextProvider = appContextProvider;
    this.gsonProvider = gsonProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public DataConverter get() {
    return provideDataConverter(appContextProvider.get(), gsonProvider.get(), watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideDataConverterFactory create(Provider<Context> appContextProvider,
      Provider<Gson> gsonProvider, Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideDataConverterFactory(appContextProvider, gsonProvider, watchDataStoreProvider);
  }

  public static DataConverter provideDataConverter(Context appContext, Gson gson,
      WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideDataConverter(appContext, gson, watchDataStore));
  }
}
