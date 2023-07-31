package com.noisefit_ryeex_sdk.handler;

import android.content.Context;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler;
import com.noisefit_ryeex_sdk.dataConversion.DataConverter;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class RyeexUpdateDeviceUnitsHandler_Factory implements Factory<RyeexUpdateDeviceUnitsHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> contextProvider;

  private final Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public RyeexUpdateDeviceUnitsHandler_Factory(Provider<DataConverter> dataConverterProvider,
      Provider<Context> contextProvider,
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.contextProvider = contextProvider;
    this.ryeexApplicationHandlerProvider = ryeexApplicationHandlerProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public RyeexUpdateDeviceUnitsHandler get() {
    return newInstance(dataConverterProvider.get(), contextProvider.get(), ryeexApplicationHandlerProvider.get(), watchDataStoreProvider.get());
  }

  public static RyeexUpdateDeviceUnitsHandler_Factory create(
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new RyeexUpdateDeviceUnitsHandler_Factory(dataConverterProvider, contextProvider, ryeexApplicationHandlerProvider, watchDataStoreProvider);
  }

  public static RyeexUpdateDeviceUnitsHandler newInstance(DataConverter dataConverter,
      Context context, RyeexApplicationHandler ryeexApplicationHandler,
      WatchDataStore watchDataStore) {
    return new RyeexUpdateDeviceUnitsHandler(dataConverter, context, ryeexApplicationHandler, watchDataStore);
  }
}
