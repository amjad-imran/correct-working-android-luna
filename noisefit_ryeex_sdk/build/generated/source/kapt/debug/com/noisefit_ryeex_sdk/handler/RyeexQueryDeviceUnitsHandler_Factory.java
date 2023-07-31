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
public final class RyeexQueryDeviceUnitsHandler_Factory implements Factory<RyeexQueryDeviceUnitsHandler> {
  private final Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> contextProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public RyeexQueryDeviceUnitsHandler_Factory(
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.ryeexApplicationHandlerProvider = ryeexApplicationHandlerProvider;
    this.dataConverterProvider = dataConverterProvider;
    this.contextProvider = contextProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public RyeexQueryDeviceUnitsHandler get() {
    return newInstance(ryeexApplicationHandlerProvider.get(), dataConverterProvider.get(), contextProvider.get(), watchDataStoreProvider.get());
  }

  public static RyeexQueryDeviceUnitsHandler_Factory create(
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new RyeexQueryDeviceUnitsHandler_Factory(ryeexApplicationHandlerProvider, dataConverterProvider, contextProvider, watchDataStoreProvider);
  }

  public static RyeexQueryDeviceUnitsHandler newInstance(
      RyeexApplicationHandler ryeexApplicationHandler, DataConverter dataConverter, Context context,
      WatchDataStore watchDataStore) {
    return new RyeexQueryDeviceUnitsHandler(ryeexApplicationHandler, dataConverter, context, watchDataStore);
  }
}
