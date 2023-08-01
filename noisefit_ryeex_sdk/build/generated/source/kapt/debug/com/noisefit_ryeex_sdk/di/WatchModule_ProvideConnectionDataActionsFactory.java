package com.noisefit_ryeex_sdk.di;

import com.noisefit_commans.interfaces.connection.ConnectionDataActions;
import com.noisefit_ryeex_sdk.handler.connect.RyeexConnectHandler;
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
  private final Provider<RyeexConnectHandler> ryeexConnectHandlerProvider;

  public WatchModule_ProvideConnectionDataActionsFactory(
      Provider<RyeexConnectHandler> ryeexConnectHandlerProvider) {
    this.ryeexConnectHandlerProvider = ryeexConnectHandlerProvider;
  }

  @Override
  public ConnectionDataActions get() {
    return provideConnectionDataActions(ryeexConnectHandlerProvider.get());
  }

  public static WatchModule_ProvideConnectionDataActionsFactory create(
      Provider<RyeexConnectHandler> ryeexConnectHandlerProvider) {
    return new WatchModule_ProvideConnectionDataActionsFactory(ryeexConnectHandlerProvider);
  }

  public static ConnectionDataActions provideConnectionDataActions(
      RyeexConnectHandler ryeexConnectHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideConnectionDataActions(ryeexConnectHandler));
  }
}
