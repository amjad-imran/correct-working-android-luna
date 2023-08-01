package com.noisefit_nav_plus.handler;

import com.noisefit_nav_plus.base.NavPlusApplicationHandler;
import com.noisefit_nav_plus.handler.dataConversion.DataConverter;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class NavPlusUserActivityHandler_Factory implements Factory<NavPlusUserActivityHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider;

  public NavPlusUserActivityHandler_Factory(Provider<DataConverter> dataConverterProvider,
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.navPlusApplicationHandlerProvider = navPlusApplicationHandlerProvider;
  }

  @Override
  public NavPlusUserActivityHandler get() {
    return newInstance(dataConverterProvider.get(), navPlusApplicationHandlerProvider.get());
  }

  public static NavPlusUserActivityHandler_Factory create(
      Provider<DataConverter> dataConverterProvider,
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider) {
    return new NavPlusUserActivityHandler_Factory(dataConverterProvider, navPlusApplicationHandlerProvider);
  }

  public static NavPlusUserActivityHandler newInstance(DataConverter dataConverter,
      NavPlusApplicationHandler navPlusApplicationHandler) {
    return new NavPlusUserActivityHandler(dataConverter, navPlusApplicationHandler);
  }
}
