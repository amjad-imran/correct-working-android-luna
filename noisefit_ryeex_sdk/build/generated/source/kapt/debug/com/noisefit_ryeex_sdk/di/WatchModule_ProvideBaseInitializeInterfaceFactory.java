package com.noisefit_ryeex_sdk.di;

import com.noisefit_commans.interfaces.base.BaseInitializeInterface;
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler;
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
public final class WatchModule_ProvideBaseInitializeInterfaceFactory implements Factory<BaseInitializeInterface> {
  private final Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider;

  public WatchModule_ProvideBaseInitializeInterfaceFactory(
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider) {
    this.ryeexApplicationHandlerProvider = ryeexApplicationHandlerProvider;
  }

  @Override
  public BaseInitializeInterface get() {
    return provideBaseInitializeInterface(ryeexApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideBaseInitializeInterfaceFactory create(
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider) {
    return new WatchModule_ProvideBaseInitializeInterfaceFactory(ryeexApplicationHandlerProvider);
  }

  public static BaseInitializeInterface provideBaseInitializeInterface(
      RyeexApplicationHandler ryeexApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideBaseInitializeInterface(ryeexApplicationHandler));
  }
}
