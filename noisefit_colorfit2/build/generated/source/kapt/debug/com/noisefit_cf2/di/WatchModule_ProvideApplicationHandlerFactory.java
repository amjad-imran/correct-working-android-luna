package com.noisefit_cf2.di;

import com.noisefit_cf2.base.ColorFit2ApplicationHandler;
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
public final class WatchModule_ProvideApplicationHandlerFactory implements Factory<ColorFit2ApplicationHandler> {
  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideApplicationHandlerFactory(
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public ColorFit2ApplicationHandler get() {
    return provideApplicationHandler(watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideApplicationHandlerFactory create(
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideApplicationHandlerFactory(watchDataStoreProvider);
  }

  public static ColorFit2ApplicationHandler provideApplicationHandler(
      WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideApplicationHandler(watchDataStore));
  }
}
