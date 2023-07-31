package com.noisefit_cf2.di;

import com.noisefit_cf2.handler.connect.CF2ConnectHandler;
import com.noisefit_commans.interfaces.connection.ConnectionDataActions;
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
  private final Provider<CF2ConnectHandler> cF2ConnectHandlerProvider;

  public WatchModule_ProvideConnectionDataActionsFactory(
      Provider<CF2ConnectHandler> cF2ConnectHandlerProvider) {
    this.cF2ConnectHandlerProvider = cF2ConnectHandlerProvider;
  }

  @Override
  public ConnectionDataActions get() {
    return provideConnectionDataActions(cF2ConnectHandlerProvider.get());
  }

  public static WatchModule_ProvideConnectionDataActionsFactory create(
      Provider<CF2ConnectHandler> cF2ConnectHandlerProvider) {
    return new WatchModule_ProvideConnectionDataActionsFactory(cF2ConnectHandlerProvider);
  }

  public static ConnectionDataActions provideConnectionDataActions(
      CF2ConnectHandler cF2ConnectHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideConnectionDataActions(cF2ConnectHandler));
  }
}
