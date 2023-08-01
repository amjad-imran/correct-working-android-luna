package com.noisefit.colorfit_pro.di;

import com.noisefit.colorfit_pro.base.ProApplicationHandler;
import com.noisefit.colorfit_pro.handler.connect.ProConnectHandler;
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
public final class WatchModule_ProvideProConnectHandlerFactory implements Factory<ProConnectHandler> {
  private final Provider<ProApplicationHandler> proApplicationHandlerProvider;

  public WatchModule_ProvideProConnectHandlerFactory(
      Provider<ProApplicationHandler> proApplicationHandlerProvider) {
    this.proApplicationHandlerProvider = proApplicationHandlerProvider;
  }

  @Override
  public ProConnectHandler get() {
    return provideProConnectHandler(proApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideProConnectHandlerFactory create(
      Provider<ProApplicationHandler> proApplicationHandlerProvider) {
    return new WatchModule_ProvideProConnectHandlerFactory(proApplicationHandlerProvider);
  }

  public static ProConnectHandler provideProConnectHandler(
      ProApplicationHandler proApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideProConnectHandler(proApplicationHandler));
  }
}
