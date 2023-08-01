package com.noisefit.hybrid.di;

import com.noisefit.hybrid.handler.connect.NFHybridConnectHandler;
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
  private final Provider<NFHybridConnectHandler> nfhConnectHandlerProvider;

  public WatchModule_ProvideConnectionDataActionsFactory(
      Provider<NFHybridConnectHandler> nfhConnectHandlerProvider) {
    this.nfhConnectHandlerProvider = nfhConnectHandlerProvider;
  }

  @Override
  public ConnectionDataActions get() {
    return provideConnectionDataActions(nfhConnectHandlerProvider.get());
  }

  public static WatchModule_ProvideConnectionDataActionsFactory create(
      Provider<NFHybridConnectHandler> nfhConnectHandlerProvider) {
    return new WatchModule_ProvideConnectionDataActionsFactory(nfhConnectHandlerProvider);
  }

  public static ConnectionDataActions provideConnectionDataActions(
      NFHybridConnectHandler nfhConnectHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideConnectionDataActions(nfhConnectHandler));
  }
}
