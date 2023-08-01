package com.noisefit_evolve2.di;

import com.noisefit_evolve2.base.Evolve2ApplicationHandler;
import com.noisefit_evolve2.handler.connect.Evolve2ConnectHandler;
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
public final class WatchModule_ProvideEvolve2ConnectHandlerFactory implements Factory<Evolve2ConnectHandler> {
  private final Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider;

  public WatchModule_ProvideEvolve2ConnectHandlerFactory(
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider) {
    this.evolve2ApplicationHandlerProvider = evolve2ApplicationHandlerProvider;
  }

  @Override
  public Evolve2ConnectHandler get() {
    return provideEvolve2ConnectHandler(evolve2ApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideEvolve2ConnectHandlerFactory create(
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider) {
    return new WatchModule_ProvideEvolve2ConnectHandlerFactory(evolve2ApplicationHandlerProvider);
  }

  public static Evolve2ConnectHandler provideEvolve2ConnectHandler(
      Evolve2ApplicationHandler evolve2ApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideEvolve2ConnectHandler(evolve2ApplicationHandler));
  }
}
