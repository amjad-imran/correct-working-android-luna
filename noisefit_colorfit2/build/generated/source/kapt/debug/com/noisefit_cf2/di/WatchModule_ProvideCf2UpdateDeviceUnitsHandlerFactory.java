package com.noisefit_cf2.di;

import com.noisefit_cf2.handler.CF2UpdateDeviceUnitsHandler;
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
public final class WatchModule_ProvideCf2UpdateDeviceUnitsHandlerFactory implements Factory<CF2UpdateDeviceUnitsHandler> {
  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideCf2UpdateDeviceUnitsHandlerFactory(
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public CF2UpdateDeviceUnitsHandler get() {
    return provideCf2UpdateDeviceUnitsHandler(watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideCf2UpdateDeviceUnitsHandlerFactory create(
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideCf2UpdateDeviceUnitsHandlerFactory(watchDataStoreProvider);
  }

  public static CF2UpdateDeviceUnitsHandler provideCf2UpdateDeviceUnitsHandler(
      WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideCf2UpdateDeviceUnitsHandler(watchDataStore));
  }
}
