package com.noisefit_cf2.di;

import com.noisefit_cf2.handler.CF2UserActivityDataHandler;
import com.noisefit_commans.interfaces.data.UserActivityDataActions;
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
  private final Provider<CF2UserActivityDataHandler> cF2UserActivityDataHandlerProvider;

  public WatchModule_ProvideUserActivityDataActionsFactory(
      Provider<CF2UserActivityDataHandler> cF2UserActivityDataHandlerProvider) {
    this.cF2UserActivityDataHandlerProvider = cF2UserActivityDataHandlerProvider;
  }

  @Override
  public UserActivityDataActions get() {
    return provideUserActivityDataActions(cF2UserActivityDataHandlerProvider.get());
  }

  public static WatchModule_ProvideUserActivityDataActionsFactory create(
      Provider<CF2UserActivityDataHandler> cF2UserActivityDataHandlerProvider) {
    return new WatchModule_ProvideUserActivityDataActionsFactory(cF2UserActivityDataHandlerProvider);
  }

  public static UserActivityDataActions provideUserActivityDataActions(
      CF2UserActivityDataHandler cF2UserActivityDataHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideUserActivityDataActions(cF2UserActivityDataHandler));
  }
}
