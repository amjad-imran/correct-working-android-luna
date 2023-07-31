package com.noisefit_nav_plus.di;

import com.noisefit_nav_plus.base.NavPlusApplicationHandler;
import com.noisefit_nav_plus.handler.NavPlusUserActivityHandler;
import com.noisefit_nav_plus.handler.dataConversion.DataConverter;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class WatchModule_ProvideNavPlusUserActivityHandlerFactory implements Factory<NavPlusUserActivityHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider;

  public WatchModule_ProvideNavPlusUserActivityHandlerFactory(
      Provider<DataConverter> dataConverterProvider,
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.navPlusApplicationHandlerProvider = navPlusApplicationHandlerProvider;
  }

  @Override
  public NavPlusUserActivityHandler get() {
    return provideNavPlusUserActivityHandler(dataConverterProvider.get(), navPlusApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideNavPlusUserActivityHandlerFactory create(
      Provider<DataConverter> dataConverterProvider,
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider) {
    return new WatchModule_ProvideNavPlusUserActivityHandlerFactory(dataConverterProvider, navPlusApplicationHandlerProvider);
  }

  public static NavPlusUserActivityHandler provideNavPlusUserActivityHandler(
      DataConverter dataConverter, NavPlusApplicationHandler navPlusApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideNavPlusUserActivityHandler(dataConverter, navPlusApplicationHandler));
  }
}
