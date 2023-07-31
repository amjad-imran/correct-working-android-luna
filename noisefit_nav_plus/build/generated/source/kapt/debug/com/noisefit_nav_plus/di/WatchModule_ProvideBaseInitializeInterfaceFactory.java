package com.noisefit_nav_plus.di;

import com.noisefit_commans.interfaces.base.BaseInitializeInterface;
import com.noisefit_nav_plus.base.NavPlusApplicationHandler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class WatchModule_ProvideBaseInitializeInterfaceFactory implements Factory<BaseInitializeInterface> {
  private final Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider;

  public WatchModule_ProvideBaseInitializeInterfaceFactory(
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider) {
    this.navPlusApplicationHandlerProvider = navPlusApplicationHandlerProvider;
  }

  @Override
  public BaseInitializeInterface get() {
    return provideBaseInitializeInterface(navPlusApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideBaseInitializeInterfaceFactory create(
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider) {
    return new WatchModule_ProvideBaseInitializeInterfaceFactory(navPlusApplicationHandlerProvider);
  }

  public static BaseInitializeInterface provideBaseInitializeInterface(
      NavPlusApplicationHandler navPlusApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideBaseInitializeInterface(navPlusApplicationHandler));
  }
}
