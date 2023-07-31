package com.noisefit.hybrid.di;

import com.noisefit.hybrid.handler.NFHUserActivityHandler;
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
  private final Provider<NFHUserActivityHandler> nfhUserActivityDataHandlerProvider;

  public WatchModule_ProvideUserActivityDataActionsFactory(
      Provider<NFHUserActivityHandler> nfhUserActivityDataHandlerProvider) {
    this.nfhUserActivityDataHandlerProvider = nfhUserActivityDataHandlerProvider;
  }

  @Override
  public UserActivityDataActions get() {
    return provideUserActivityDataActions(nfhUserActivityDataHandlerProvider.get());
  }

  public static WatchModule_ProvideUserActivityDataActionsFactory create(
      Provider<NFHUserActivityHandler> nfhUserActivityDataHandlerProvider) {
    return new WatchModule_ProvideUserActivityDataActionsFactory(nfhUserActivityDataHandlerProvider);
  }

  public static UserActivityDataActions provideUserActivityDataActions(
      NFHUserActivityHandler nfhUserActivityDataHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideUserActivityDataActions(nfhUserActivityDataHandler));
  }
}
