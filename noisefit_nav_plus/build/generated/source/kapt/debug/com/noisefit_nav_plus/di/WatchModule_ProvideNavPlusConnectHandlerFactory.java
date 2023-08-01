package com.noisefit_nav_plus.di;

import com.noisefit_nav_plus.base.NavPlusApplicationHandler;
import com.noisefit_nav_plus.handler.connect.NavPlusConnectHandler;
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
public final class WatchModule_ProvideNavPlusConnectHandlerFactory implements Factory<NavPlusConnectHandler> {
  private final Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider;

  public WatchModule_ProvideNavPlusConnectHandlerFactory(
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider) {
    this.navPlusApplicationHandlerProvider = navPlusApplicationHandlerProvider;
  }

  @Override
  public NavPlusConnectHandler get() {
    return provideNavPlusConnectHandler(navPlusApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideNavPlusConnectHandlerFactory create(
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider) {
    return new WatchModule_ProvideNavPlusConnectHandlerFactory(navPlusApplicationHandlerProvider);
  }

  public static NavPlusConnectHandler provideNavPlusConnectHandler(
      NavPlusApplicationHandler navPlusApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideNavPlusConnectHandler(navPlusApplicationHandler));
  }
}
