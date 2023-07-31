package com.noisefit.colorfit_pro.di;

import com.noisefit.colorfit_pro.base.ProApplicationHandler;
import com.noisefit_commans.interfaces.base.BaseInitializeInterface;
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
  private final Provider<ProApplicationHandler> proApplicationHandlerProvider;

  public WatchModule_ProvideBaseInitializeInterfaceFactory(
      Provider<ProApplicationHandler> proApplicationHandlerProvider) {
    this.proApplicationHandlerProvider = proApplicationHandlerProvider;
  }

  @Override
  public BaseInitializeInterface get() {
    return provideBaseInitializeInterface(proApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideBaseInitializeInterfaceFactory create(
      Provider<ProApplicationHandler> proApplicationHandlerProvider) {
    return new WatchModule_ProvideBaseInitializeInterfaceFactory(proApplicationHandlerProvider);
  }

  public static BaseInitializeInterface provideBaseInitializeInterface(
      ProApplicationHandler proApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideBaseInitializeInterface(proApplicationHandler));
  }
}
