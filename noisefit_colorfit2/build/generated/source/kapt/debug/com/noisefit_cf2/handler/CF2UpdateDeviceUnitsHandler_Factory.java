package com.noisefit_cf2.handler;

import com.noisefit_commans.data.local.abstraction.WatchDataStore;
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
public final class CF2UpdateDeviceUnitsHandler_Factory implements Factory<CF2UpdateDeviceUnitsHandler> {
  private final Provider<WatchDataStore> watchDataStoreProvider;

  public CF2UpdateDeviceUnitsHandler_Factory(Provider<WatchDataStore> watchDataStoreProvider) {
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public CF2UpdateDeviceUnitsHandler get() {
    return newInstance(watchDataStoreProvider.get());
  }

  public static CF2UpdateDeviceUnitsHandler_Factory create(
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new CF2UpdateDeviceUnitsHandler_Factory(watchDataStoreProvider);
  }

  public static CF2UpdateDeviceUnitsHandler newInstance(WatchDataStore watchDataStore) {
    return new CF2UpdateDeviceUnitsHandler(watchDataStore);
  }
}
