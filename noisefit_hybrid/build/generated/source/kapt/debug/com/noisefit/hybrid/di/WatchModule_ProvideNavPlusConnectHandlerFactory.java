package com.noisefit.hybrid.di;

import com.noisefit.hybrid.base.NFHybridApplicationHandler;
import com.noisefit.hybrid.handler.connect.NFHybridConnectHandler;
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
public final class WatchModule_ProvideNavPlusConnectHandlerFactory implements Factory<NFHybridConnectHandler> {
  private final Provider<NFHybridApplicationHandler> applicationHandlerProvider;

  public WatchModule_ProvideNavPlusConnectHandlerFactory(
      Provider<NFHybridApplicationHandler> applicationHandlerProvider) {
    this.applicationHandlerProvider = applicationHandlerProvider;
  }

  @Override
  public NFHybridConnectHandler get() {
    return provideNavPlusConnectHandler(applicationHandlerProvider.get());
  }

  public static WatchModule_ProvideNavPlusConnectHandlerFactory create(
      Provider<NFHybridApplicationHandler> applicationHandlerProvider) {
    return new WatchModule_ProvideNavPlusConnectHandlerFactory(applicationHandlerProvider);
  }

  public static NFHybridConnectHandler provideNavPlusConnectHandler(
      NFHybridApplicationHandler applicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideNavPlusConnectHandler(applicationHandler));
  }
}
