package com.noisefit_ryeex_sdk.di;

import android.content.Context;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler;
import com.noisefit_ryeex_sdk.dataConversion.DataConverter;
import com.noisefit_ryeex_sdk.handler.RyeexQueryDeviceUnitsHandler;
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
public final class WatchModule_ProvideRyeexQueryDeviceUnitsHandlerFactory implements Factory<RyeexQueryDeviceUnitsHandler> {
  private final Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> contextProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideRyeexQueryDeviceUnitsHandlerFactory(
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
    return provideRyeexQueryDeviceUnitsHandler(ryeexApplicationHandlerProvider.get(), dataConverterProvider.get(), contextProvider.get(), watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideRyeexQueryDeviceUnitsHandlerFactory create(
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideRyeexQueryDeviceUnitsHandlerFactory(ryeexApplicationHandlerProvider, dataConverterProvider, contextProvider, watchDataStoreProvider);
  }

  public static RyeexQueryDeviceUnitsHandler provideRyeexQueryDeviceUnitsHandler(
      RyeexApplicationHandler ryeexApplicationHandler, DataConverter dataConverter, Context context,
      WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideRyeexQueryDeviceUnitsHandler(ryeexApplicationHandler, dataConverter, context, watchDataStore));
  }
}
