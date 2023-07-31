package com.noisefit_nav_plus.di;

import com.noisefit_commans.interfaces.data.UserActivityDataActions;
import com.noisefit_nav_plus.handler.NavPlusUserActivityHandler;
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
public final class WatchModule_ProvideUserActivityDataActionsFactory implements Factory<UserActivityDataActions> {
  private final Provider<NavPlusUserActivityHandler> navPlusUserActivityHandlerProvider;

  public WatchModule_ProvideUserActivityDataActionsFactory(
      Provider<NavPlusUserActivityHandler> navPlusUserActivityHandlerProvider) {
    this.navPlusUserActivityHandlerProvider = navPlusUserActivityHandlerProvider;
  }

  @Override
  public UserActivityDataActions get() {
    return provideUserActivityDataActions(navPlusUserActivityHandlerProvider.get());
  }

  public static WatchModule_ProvideUserActivityDataActionsFactory create(
      Provider<NavPlusUserActivityHandler> navPlusUserActivityHandlerProvider) {
    return new WatchModule_ProvideUserActivityDataActionsFactory(navPlusUserActivityHandlerProvider);
  }

  public static UserActivityDataActions provideUserActivityDataActions(
      NavPlusUserActivityHandler navPlusUserActivityHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideUserActivityDataActions(navPlusUserActivityHandler));
  }
}
