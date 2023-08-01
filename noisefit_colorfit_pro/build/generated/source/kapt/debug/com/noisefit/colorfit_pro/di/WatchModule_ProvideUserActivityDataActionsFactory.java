package com.noisefit.colorfit_pro.di;

import com.noisefit.colorfit_pro.handler.ProUserActivityHandler;
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
  private final Provider<ProUserActivityHandler> proUserActivityHandlerProvider;

  public WatchModule_ProvideUserActivityDataActionsFactory(
      Provider<ProUserActivityHandler> proUserActivityHandlerProvider) {
    this.proUserActivityHandlerProvider = proUserActivityHandlerProvider;
  }

  @Override
  public UserActivityDataActions get() {
    return provideUserActivityDataActions(proUserActivityHandlerProvider.get());
  }

  public static WatchModule_ProvideUserActivityDataActionsFactory create(
      Provider<ProUserActivityHandler> proUserActivityHandlerProvider) {
    return new WatchModule_ProvideUserActivityDataActionsFactory(proUserActivityHandlerProvider);
  }

  public static UserActivityDataActions provideUserActivityDataActions(
      ProUserActivityHandler proUserActivityHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideUserActivityDataActions(proUserActivityHandler));
  }
}
