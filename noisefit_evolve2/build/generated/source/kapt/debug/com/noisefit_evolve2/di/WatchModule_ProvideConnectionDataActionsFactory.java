package com.noisefit_evolve2.di;

import com.noisefit_commans.interfaces.connection.ConnectionDataActions;
import com.noisefit_evolve2.handler.connect.Evolve2ConnectHandler;
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
public final class WatchModule_ProvideConnectionDataActionsFactory implements Factory<ConnectionDataActions> {
  private final Provider<Evolve2ConnectHandler> evolve2ConnectHandlerProvider;

  public WatchModule_ProvideConnectionDataActionsFactory(
      Provider<Evolve2ConnectHandler> evolve2ConnectHandlerProvider) {
    this.evolve2ConnectHandlerProvider = evolve2ConnectHandlerProvider;
  }

  @Override
  public ConnectionDataActions get() {
    return provideConnectionDataActions(evolve2ConnectHandlerProvider.get());
  }

  public static WatchModule_ProvideConnectionDataActionsFactory create(
      Provider<Evolve2ConnectHandler> evolve2ConnectHandlerProvider) {
    return new WatchModule_ProvideConnectionDataActionsFactory(evolve2ConnectHandlerProvider);
  }

  public static ConnectionDataActions provideConnectionDataActions(
      Evolve2ConnectHandler evolve2ConnectHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideConnectionDataActions(evolve2ConnectHandler));
  }
}
