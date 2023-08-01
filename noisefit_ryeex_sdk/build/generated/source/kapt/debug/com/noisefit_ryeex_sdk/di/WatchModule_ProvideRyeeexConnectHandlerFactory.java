package com.noisefit_ryeex_sdk.di;

import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler;
import com.noisefit_ryeex_sdk.handler.connect.RyeexConnectHandler;
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
public final class WatchModule_ProvideRyeeexConnectHandlerFactory implements Factory<RyeexConnectHandler> {
  private final Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideRyeeexConnectHandlerFactory(
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.ryeexApplicationHandlerProvider = ryeexApplicationHandlerProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public RyeexConnectHandler get() {
    return provideRyeeexConnectHandler(ryeexApplicationHandlerProvider.get(), watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideRyeeexConnectHandlerFactory create(
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideRyeeexConnectHandlerFactory(ryeexApplicationHandlerProvider, watchDataStoreProvider);
  }

  public static RyeexConnectHandler provideRyeeexConnectHandler(
      RyeexApplicationHandler ryeexApplicationHandler, WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideRyeeexConnectHandler(ryeexApplicationHandler, watchDataStore));
  }
}
