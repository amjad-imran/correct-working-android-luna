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
public final class Evolve2QueryDeviceUnitsHandler_Factory implements Factory<Evolve2QueryDeviceUnitsHandler> {
  private final Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> contextProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public Evolve2QueryDeviceUnitsHandler_Factory(
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.evolve2ApplicationHandlerProvider = evolve2ApplicationHandlerProvider;
    this.dataConverterProvider = dataConverterProvider;
    this.contextProvider = contextProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public Evolve2QueryDeviceUnitsHandler get() {
    return newInstance(evolve2ApplicationHandlerProvider.get(), dataConverterProvider.get(), contextProvider.get(), watchDataStoreProvider.get());
  }

  public static Evolve2QueryDeviceUnitsHandler_Factory create(
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new Evolve2QueryDeviceUnitsHandler_Factory(evolve2ApplicationHandlerProvider, dataConverterProvider, contextProvider, watchDataStoreProvider);
  }

  public static Evolve2QueryDeviceUnitsHandler newInstance(
      Evolve2ApplicationHandler evolve2ApplicationHandler, DataConverter dataConverter,
      Context context, WatchDataStore watchDataStore) {
    return new Evolve2QueryDeviceUnitsHandler(evolve2ApplicationHandler, dataConverter, context, watchDataStore);
  }
}
