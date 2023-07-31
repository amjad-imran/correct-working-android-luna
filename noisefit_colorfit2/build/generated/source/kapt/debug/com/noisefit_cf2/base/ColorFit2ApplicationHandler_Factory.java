package com.noisefit_cf2.base;

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
public final class ColorFit2ApplicationHandler_Factory implements Factory<ColorFit2ApplicationHandler> {
  private final Provider<WatchDataStore> watchDataStoreProvider;

  public ColorFit2ApplicationHandler_Factory(Provider<WatchDataStore> watchDataStoreProvider) {
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public ColorFit2ApplicationHandler get() {
    return newInstance(watchDataStoreProvider.get());
  }

  public static ColorFit2ApplicationHandler_Factory create(
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new ColorFit2ApplicationHandler_Factory(watchDataStoreProvider);
  }

  public static ColorFit2ApplicationHandler newInstance(WatchDataStore watchDataStore) {
    return new ColorFit2ApplicationHandler(watchDataStore);
  }
}
