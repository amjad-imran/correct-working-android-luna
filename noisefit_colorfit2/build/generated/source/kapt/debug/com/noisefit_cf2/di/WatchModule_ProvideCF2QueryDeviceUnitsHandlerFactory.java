package com.noisefit_cf2.di;

import com.noisefit_cf2.handler.CF2QueryDeviceUnitsHandler;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class WatchModule_ProvideCF2QueryDeviceUnitsHandlerFactory implements Factory<CF2QueryDeviceUnitsHandler> {
  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideCF2QueryDeviceUnitsHandlerFactory(
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public CF2QueryDeviceUnitsHandler get() {
    return provideCF2QueryDeviceUnitsHandler(watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideCF2QueryDeviceUnitsHandlerFactory create(
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideCF2QueryDeviceUnitsHandlerFactory(watchDataStoreProvider);
  }

  public static CF2QueryDeviceUnitsHandler provideCF2QueryDeviceUnitsHandler(
      WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideCF2QueryDeviceUnitsHandler(watchDataStore));
  }
}
