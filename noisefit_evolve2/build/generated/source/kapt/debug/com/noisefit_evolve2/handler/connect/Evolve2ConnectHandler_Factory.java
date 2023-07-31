package com.noisefit_evolve2.handler.connect;

import com.noisefit_evolve2.base.Evolve2ApplicationHandler;
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
public final class Evolve2ConnectHandler_Factory implements Factory<Evolve2ConnectHandler> {
  private final Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider;

  public Evolve2ConnectHandler_Factory(
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider) {
    this.evolve2ApplicationHandlerProvider = evolve2ApplicationHandlerProvider;
  }

  @Override
  public Evolve2ConnectHandler get() {
    return newInstance(evolve2ApplicationHandlerProvider.get());
  }

  public static Evolve2ConnectHandler_Factory create(
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider) {
    return new Evolve2ConnectHandler_Factory(evolve2ApplicationHandlerProvider);
  }

  public static Evolve2ConnectHandler newInstance(
      Evolve2ApplicationHandler evolve2ApplicationHandler) {
    return new Evolve2ConnectHandler(evolve2ApplicationHandler);
  }
}
