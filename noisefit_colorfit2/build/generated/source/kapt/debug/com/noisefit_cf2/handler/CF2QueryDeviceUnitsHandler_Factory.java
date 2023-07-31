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
public final class CF2QueryDeviceUnitsHandler_Factory implements Factory<CF2QueryDeviceUnitsHandler> {
  private final Provider<WatchDataStore> watchDataStoreProvider;

  public CF2QueryDeviceUnitsHandler_Factory(Provider<WatchDataStore> watchDataStoreProvider) {
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public CF2QueryDeviceUnitsHandler get() {
    return newInstance(watchDataStoreProvider.get());
  }

  public static CF2QueryDeviceUnitsHandler_Factory create(
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new CF2QueryDeviceUnitsHandler_Factory(watchDataStoreProvider);
  }

  public static CF2QueryDeviceUnitsHandler newInstance(WatchDataStore watchDataStore) {
    return new CF2QueryDeviceUnitsHandler(watchDataStore);
  }
}
