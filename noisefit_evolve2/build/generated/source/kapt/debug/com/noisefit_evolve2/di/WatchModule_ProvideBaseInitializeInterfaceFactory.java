package com.noisefit_evolve2.di;

import com.noisefit_commans.interfaces.base.BaseInitializeInterface;
import com.noisefit_evolve2.base.Evolve2ApplicationHandler;
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
  private final Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider;

  public WatchModule_ProvideBaseInitializeInterfaceFactory(
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider) {
    this.evolve2ApplicationHandlerProvider = evolve2ApplicationHandlerProvider;
  }

  @Override
  public BaseInitializeInterface get() {
    return provideBaseInitializeInterface(evolve2ApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideBaseInitializeInterfaceFactory create(
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider) {
    return new WatchModule_ProvideBaseInitializeInterfaceFactory(evolve2ApplicationHandlerProvider);
  }

  public static BaseInitializeInterface provideBaseInitializeInterface(
      Evolve2ApplicationHandler evolve2ApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideBaseInitializeInterface(evolve2ApplicationHandler));
  }
}
