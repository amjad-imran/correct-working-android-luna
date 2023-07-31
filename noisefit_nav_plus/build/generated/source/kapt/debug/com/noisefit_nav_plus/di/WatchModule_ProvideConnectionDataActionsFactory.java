package com.noisefit_nav_plus.di;

import com.noisefit_commans.interfaces.connection.ConnectionDataActions;
import com.noisefit_nav_plus.handler.connect.NavPlusConnectHandler;
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
  private final Provider<NavPlusConnectHandler> navPlusConnectHandlerProvider;

  public WatchModule_ProvideConnectionDataActionsFactory(
      Provider<NavPlusConnectHandler> navPlusConnectHandlerProvider) {
    this.navPlusConnectHandlerProvider = navPlusConnectHandlerProvider;
  }

  @Override
  public ConnectionDataActions get() {
    return provideConnectionDataActions(navPlusConnectHandlerProvider.get());
  }

  public static WatchModule_ProvideConnectionDataActionsFactory create(
      Provider<NavPlusConnectHandler> navPlusConnectHandlerProvider) {
    return new WatchModule_ProvideConnectionDataActionsFactory(navPlusConnectHandlerProvider);
  }

  public static ConnectionDataActions provideConnectionDataActions(
      NavPlusConnectHandler navPlusConnectHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideConnectionDataActions(navPlusConnectHandler));
  }
}
