package com.noisefit_ryeex_sdk.handler.connect;

import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler;
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
public final class RyeexConnectHandler_Factory implements Factory<RyeexConnectHandler> {
  private final Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public RyeexConnectHandler_Factory(
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.ryeexApplicationHandlerProvider = ryeexApplicationHandlerProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public RyeexConnectHandler get() {
    return newInstance(ryeexApplicationHandlerProvider.get(), watchDataStoreProvider.get());
  }

  public static RyeexConnectHandler_Factory create(
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new RyeexConnectHandler_Factory(ryeexApplicationHandlerProvider, watchDataStoreProvider);
  }

  public static RyeexConnectHandler newInstance(RyeexApplicationHandler ryeexApplicationHandler,
      WatchDataStore watchDataStore) {
    return new RyeexConnectHandler(ryeexApplicationHandler, watchDataStore);
  }
}
