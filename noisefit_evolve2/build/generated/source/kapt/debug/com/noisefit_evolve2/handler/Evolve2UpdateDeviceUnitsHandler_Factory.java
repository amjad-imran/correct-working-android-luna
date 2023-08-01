package com.noisefit_evolve2.handler;

import android.content.Context;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import com.noisefit_evolve2.base.Evolve2ApplicationHandler;
import com.noisefit_evolve2.dataConversion.DataConverter;
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
public final class Evolve2UpdateDeviceUnitsHandler_Factory implements Factory<Evolve2UpdateDeviceUnitsHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> contextProvider;

  private final Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public Evolve2UpdateDeviceUnitsHandler_Factory(Provider<DataConverter> dataConverterProvider,
      Provider<Context> contextProvider,
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.contextProvider = contextProvider;
    this.evolve2ApplicationHandlerProvider = evolve2ApplicationHandlerProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public Evolve2UpdateDeviceUnitsHandler get() {
    return newInstance(dataConverterProvider.get(), contextProvider.get(), evolve2ApplicationHandlerProvider.get(), watchDataStoreProvider.get());
  }

  public static Evolve2UpdateDeviceUnitsHandler_Factory create(
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new Evolve2UpdateDeviceUnitsHandler_Factory(dataConverterProvider, contextProvider, evolve2ApplicationHandlerProvider, watchDataStoreProvider);
  }

  public static Evolve2UpdateDeviceUnitsHandler newInstance(DataConverter dataConverter,
      Context context, Evolve2ApplicationHandler evolve2ApplicationHandler,
      WatchDataStore watchDataStore) {
    return new Evolve2UpdateDeviceUnitsHandler(dataConverter, context, evolve2ApplicationHandler, watchDataStore);
  }
}
