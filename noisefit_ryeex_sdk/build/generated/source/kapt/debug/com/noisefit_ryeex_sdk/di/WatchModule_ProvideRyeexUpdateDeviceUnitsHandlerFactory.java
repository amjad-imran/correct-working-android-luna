package com.noisefit_ryeex_sdk.di;

import android.content.Context;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler;
import com.noisefit_ryeex_sdk.dataConversion.DataConverter;
import com.noisefit_ryeex_sdk.handler.RyeexUpdateDeviceUnitsHandler;
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
public final class WatchModule_ProvideRyeexUpdateDeviceUnitsHandlerFactory implements Factory<RyeexUpdateDeviceUnitsHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> appContextProvider;

  private final Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideRyeexUpdateDeviceUnitsHandlerFactory(
      Provider<DataConverter> dataConverterProvider, Provider<Context> appContextProvider,
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.appContextProvider = appContextProvider;
    this.ryeexApplicationHandlerProvider = ryeexApplicationHandlerProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public RyeexUpdateDeviceUnitsHandler get() {
    return provideRyeexUpdateDeviceUnitsHandler(dataConverterProvider.get(), appContextProvider.get(), ryeexApplicationHandlerProvider.get(), watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideRyeexUpdateDeviceUnitsHandlerFactory create(
      Provider<DataConverter> dataConverterProvider, Provider<Context> appContextProvider,
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideRyeexUpdateDeviceUnitsHandlerFactory(dataConverterProvider, appContextProvider, ryeexApplicationHandlerProvider, watchDataStoreProvider);
  }

  public static RyeexUpdateDeviceUnitsHandler provideRyeexUpdateDeviceUnitsHandler(
      DataConverter dataConverter, Context appContext,
      RyeexApplicationHandler ryeexApplicationHandler, WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideRyeexUpdateDeviceUnitsHandler(dataConverter, appContext, ryeexApplicationHandler, watchDataStore));
  }
}
