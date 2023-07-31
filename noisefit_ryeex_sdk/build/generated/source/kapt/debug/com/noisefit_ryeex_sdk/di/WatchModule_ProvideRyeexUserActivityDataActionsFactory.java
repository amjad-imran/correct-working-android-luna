package com.noisefit_ryeex_sdk.di;

import com.noisefit_commans.interfaces.data.UserActivityDataActions;
import com.noisefit_ryeex_sdk.handler.RyeexUserActivityHandler;
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
public final class WatchModule_ProvideRyeexUserActivityDataActionsFactory implements Factory<UserActivityDataActions> {
  private final Provider<RyeexUserActivityHandler> ryeexUserActivityHandlerProvider;

  public WatchModule_ProvideRyeexUserActivityDataActionsFactory(
      Provider<RyeexUserActivityHandler> ryeexUserActivityHandlerProvider) {
    this.ryeexUserActivityHandlerProvider = ryeexUserActivityHandlerProvider;
  }

  @Override
  public UserActivityDataActions get() {
    return provideRyeexUserActivityDataActions(ryeexUserActivityHandlerProvider.get());
  }

  public static WatchModule_ProvideRyeexUserActivityDataActionsFactory create(
      Provider<RyeexUserActivityHandler> ryeexUserActivityHandlerProvider) {
    return new WatchModule_ProvideRyeexUserActivityDataActionsFactory(ryeexUserActivityHandlerProvider);
  }

  public static UserActivityDataActions provideRyeexUserActivityDataActions(
      RyeexUserActivityHandler ryeexUserActivityHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideRyeexUserActivityDataActions(ryeexUserActivityHandler));
  }
}
