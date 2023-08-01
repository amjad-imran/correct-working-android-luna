package com.noisefit_cf2.di;

import com.noisefit_cf2.base.ColorFit2ApplicationHandler;
import com.noisefit_cf2.handler.connect.CF2ConnectHandler;
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
public final class WatchModule_ProvideNavPlusConnectHandlerFactory implements Factory<CF2ConnectHandler> {
  private final Provider<ColorFit2ApplicationHandler> applicationHandlerProvider;

  public WatchModule_ProvideNavPlusConnectHandlerFactory(
      Provider<ColorFit2ApplicationHandler> applicationHandlerProvider) {
    this.applicationHandlerProvider = applicationHandlerProvider;
  }

  @Override
  public CF2ConnectHandler get() {
    return provideNavPlusConnectHandler(applicationHandlerProvider.get());
  }

  public static WatchModule_ProvideNavPlusConnectHandlerFactory create(
      Provider<ColorFit2ApplicationHandler> applicationHandlerProvider) {
    return new WatchModule_ProvideNavPlusConnectHandlerFactory(applicationHandlerProvider);
  }

  public static CF2ConnectHandler provideNavPlusConnectHandler(
      ColorFit2ApplicationHandler applicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideNavPlusConnectHandler(applicationHandler));
  }
}
