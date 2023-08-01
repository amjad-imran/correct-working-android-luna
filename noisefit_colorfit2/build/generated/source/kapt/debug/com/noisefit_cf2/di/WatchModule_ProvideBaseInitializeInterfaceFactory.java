package com.noisefit_cf2.di;

import com.noisefit_cf2.base.ColorFit2ApplicationHandler;
import com.noisefit_commans.interfaces.base.BaseInitializeInterface;
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
  private final Provider<ColorFit2ApplicationHandler> handlerProvider;

  public WatchModule_ProvideBaseInitializeInterfaceFactory(
      Provider<ColorFit2ApplicationHandler> handlerProvider) {
    this.handlerProvider = handlerProvider;
  }

  @Override
  public BaseInitializeInterface get() {
    return provideBaseInitializeInterface(handlerProvider.get());
  }

  public static WatchModule_ProvideBaseInitializeInterfaceFactory create(
      Provider<ColorFit2ApplicationHandler> handlerProvider) {
    return new WatchModule_ProvideBaseInitializeInterfaceFactory(handlerProvider);
  }

  public static BaseInitializeInterface provideBaseInitializeInterface(
      ColorFit2ApplicationHandler handler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideBaseInitializeInterface(handler));
  }
}
