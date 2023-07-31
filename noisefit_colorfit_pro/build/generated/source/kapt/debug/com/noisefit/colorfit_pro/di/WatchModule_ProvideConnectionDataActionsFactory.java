package com.noisefit.colorfit_pro.di;

import com.noisefit.colorfit_pro.handler.connect.ProConnectHandler;
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
  private final Provider<ProConnectHandler> proConnectHandlerProvider;

  public WatchModule_ProvideConnectionDataActionsFactory(
      Provider<ProConnectHandler> proConnectHandlerProvider) {
    this.proConnectHandlerProvider = proConnectHandlerProvider;
  }

  @Override
  public ConnectionDataActions get() {
    return provideConnectionDataActions(proConnectHandlerProvider.get());
  }

  public static WatchModule_ProvideConnectionDataActionsFactory create(
      Provider<ProConnectHandler> proConnectHandlerProvider) {
    return new WatchModule_ProvideConnectionDataActionsFactory(proConnectHandlerProvider);
  }

  public static ConnectionDataActions provideConnectionDataActions(
      ProConnectHandler proConnectHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideConnectionDataActions(proConnectHandler));
  }
}
