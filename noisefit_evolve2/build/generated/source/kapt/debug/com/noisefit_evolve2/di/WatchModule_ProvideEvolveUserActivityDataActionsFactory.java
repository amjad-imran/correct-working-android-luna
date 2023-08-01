package com.noisefit_evolve2.di;

import com.noisefit_commans.interfaces.data.UserActivityDataActions;
import com.noisefit_evolve2.handler.Evolve2UserActivityHandler;
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
public final class WatchModule_ProvideEvolveUserActivityDataActionsFactory implements Factory<UserActivityDataActions> {
  private final Provider<Evolve2UserActivityHandler> evolve2UserActivityHandlerProvider;

  public WatchModule_ProvideEvolveUserActivityDataActionsFactory(
      Provider<Evolve2UserActivityHandler> evolve2UserActivityHandlerProvider) {
    this.evolve2UserActivityHandlerProvider = evolve2UserActivityHandlerProvider;
  }

  @Override
  public UserActivityDataActions get() {
    return provideEvolveUserActivityDataActions(evolve2UserActivityHandlerProvider.get());
  }

  public static WatchModule_ProvideEvolveUserActivityDataActionsFactory create(
      Provider<Evolve2UserActivityHandler> evolve2UserActivityHandlerProvider) {
    return new WatchModule_ProvideEvolveUserActivityDataActionsFactory(evolve2UserActivityHandlerProvider);
  }

  public static UserActivityDataActions provideEvolveUserActivityDataActions(
      Evolve2UserActivityHandler evolve2UserActivityHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideEvolveUserActivityDataActions(evolve2UserActivityHandler));
  }
}
