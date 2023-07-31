package com.noisefit_nav_plus.handler.connect;

import com.noisefit_nav_plus.base.NavPlusApplicationHandler;
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
public final class NavPlusConnectHandler_Factory implements Factory<NavPlusConnectHandler> {
  private final Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider;

  public NavPlusConnectHandler_Factory(
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider) {
    this.navPlusApplicationHandlerProvider = navPlusApplicationHandlerProvider;
  }

  @Override
  public NavPlusConnectHandler get() {
    return newInstance(navPlusApplicationHandlerProvider.get());
  }

  public static NavPlusConnectHandler_Factory create(
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider) {
    return new NavPlusConnectHandler_Factory(navPlusApplicationHandlerProvider);
  }

  public static NavPlusConnectHandler newInstance(
      NavPlusApplicationHandler navPlusApplicationHandler) {
    return new NavPlusConnectHandler(navPlusApplicationHandler);
  }
}
