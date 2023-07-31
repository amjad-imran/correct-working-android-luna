package com.noisefit.hybrid.di;

import com.noisefit.hybrid.base.NFHybridApplicationHandler;
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
  private final Provider<NFHybridApplicationHandler> handlerProvider;

  public WatchModule_ProvideBaseInitializeInterfaceFactory(
      Provider<NFHybridApplicationHandler> handlerProvider) {
    this.handlerProvider = handlerProvider;
  }

  @Override
  public BaseInitializeInterface get() {
    return provideBaseInitializeInterface(handlerProvider.get());
  }

  public static WatchModule_ProvideBaseInitializeInterfaceFactory create(
      Provider<NFHybridApplicationHandler> handlerProvider) {
    return new WatchModule_ProvideBaseInitializeInterfaceFactory(handlerProvider);
  }

  public static BaseInitializeInterface provideBaseInitializeInterface(
      NFHybridApplicationHandler handler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideBaseInitializeInterface(handler));
  }
}
