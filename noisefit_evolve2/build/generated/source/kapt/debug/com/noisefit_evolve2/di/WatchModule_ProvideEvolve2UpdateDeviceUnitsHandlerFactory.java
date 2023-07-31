package com.noisefit_evolve2.di;

import android.content.Context;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import com.noisefit_evolve2.base.Evolve2ApplicationHandler;
import com.noisefit_evolve2.dataConversion.DataConverter;
import com.noisefit_evolve2.handler.Evolve2UpdateDeviceUnitsHandler;
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
public final class WatchModule_ProvideEvolve2UpdateDeviceUnitsHandlerFactory implements Factory<Evolve2UpdateDeviceUnitsHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> appContextProvider;

  private final Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideEvolve2UpdateDeviceUnitsHandlerFactory(
      Provider<DataConverter> dataConverterProvider, Provider<Context> appContextProvider,
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.appContextProvider = appContextProvider;
    this.evolve2ApplicationHandlerProvider = evolve2ApplicationHandlerProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public Evolve2UpdateDeviceUnitsHandler get() {
    return provideEvolve2UpdateDeviceUnitsHandler(dataConverterProvider.get(), appContextProvider.get(), evolve2ApplicationHandlerProvider.get(), watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideEvolve2UpdateDeviceUnitsHandlerFactory create(
      Provider<DataConverter> dataConverterProvider, Provider<Context> appContextProvider,
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideEvolve2UpdateDeviceUnitsHandlerFactory(dataConverterProvider, appContextProvider, evolve2ApplicationHandlerProvider, watchDataStoreProvider);
  }

  public static Evolve2UpdateDeviceUnitsHandler provideEvolve2UpdateDeviceUnitsHandler(
      DataConverter dataConverter, Context appContext,
      Evolve2ApplicationHandler evolve2ApplicationHandler, WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideEvolve2UpdateDeviceUnitsHandler(dataConverter, appContext, evolve2ApplicationHandler, watchDataStore));
  }
}
